/*
 * Copyright (c) 2025 The Contributors to Eclipse OpenSOVD (see CONTRIBUTORS)
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: Apache-2.0
 */

import schema.odx.DATAOBJECTPROP
import schema.odx.DOPBASE
import schema.odx.DTCDOP
import schema.odx.DYNAMICENDMARKERFIELD
import schema.odx.DYNAMICLENGTHFIELD
import schema.odx.ENVDATA
import schema.odx.ENVDATADESC
import schema.odx.FIELD
import schema.odx.MUX
import schema.odx.STRUCTURE

class DopSerializationOrder(private val odx: ODXCollection) {
    private val lookupMap = mutableMapOf<DOPBASE, Node>()

    init {
        // create a graph for all DOPs
        odx.combinedDataObjectProps.values.forEach {
            addToGraph(it)
        }
    }


    private class TraverseTree() {
        val order = mutableListOf<Node>()
        val visitedNodes = mutableSetOf<Node>()

        fun traverse(node: Node) {
            if (visitedNodes.contains(node)) {
                return
            }
            node.depends.forEach {
                traverse(it)
            }
            order.add(node)
            visitedNodes.add(node)
        }
    }

    fun serializationOrder(): List<Node> {
        val traverse = TraverseTree()
        lookupMap.values.forEach {
            traverse.traverse(it)
        }
        return traverse.order
    }

    private fun dop(id: String): DOPBASE =
        odx.combinedDataObjectProps[id] ?: throw IllegalArgumentException("DOP $id not found")

    private fun addToGraph(dop: DOPBASE): Node {
        var node = lookupMap[dop]
        if (node != null) {
            return node
        }
        node = Node(dop)
        lookupMap[dop] = node
        when (dop) {
            is DATAOBJECTPROP, is STRUCTURE, is ENVDATA -> {
                // DATAOBJECTPROP no dependencies
                // STRUCTURE depends on params, which we can't model with objects
                // ENVDATA has no dependencies
            }
            is ENVDATADESC -> {
                val depends = dop.envdatarefs?.envdataref?.map {
                    addToGraph(dop(it.idref))
                } ?: emptyList()
                node.depends.addAll(depends)
            }
            is DTCDOP -> {
                val depends = dop.linkeddtcdops?.linkeddtcdop?.mapNotNull { it.dtcdopref?.idref }?.map {
                    addToGraph(dop(it))
                } ?: emptyList()
                node.depends.addAll(depends)
            }
            is FIELD -> {
                val depends = listOfNotNull(
                    dop.basicstructureref?.idref?.let {
                        dop(it)
                    },
                    dop.envdatadescref?.idref?.let {
                        dop(it)
                    }
                ).map { addToGraph(it) }.toMutableList()

                if (dop is DYNAMICENDMARKERFIELD) {
                    dop.dataobjectpropref?.idref?.let {
                        depends.add(addToGraph(dop(it)))
                    }
                } else if (dop is DYNAMICLENGTHFIELD) {
                    dop.determinenumberofitems?.dataobjectpropref?.idref?.let {
                        depends.add(addToGraph(dop(it)))
                    }
                }
                node.depends.addAll(depends)
            }
            is MUX -> {
                val depends = dop.cases?.case?.mapNotNull { it.structureref?.idref }?.map { addToGraph(dop(it)) }?.toMutableList() ?: mutableListOf()
                dop.switchkey?.dataobjectpropref?.idref?.let { depends.add(addToGraph(dop(it))) }
                dop.defaultcase?.structureref?.idref?.let { depends.add(addToGraph(dop(it))) }
            }
            else -> error("Unsupported DOP type ${dop.javaClass.simpleName}")
        }
        return node
    }

    data class Node(
        val dop: DOPBASE,
        val depends: MutableSet<Node> = mutableSetOf(),
    )
}