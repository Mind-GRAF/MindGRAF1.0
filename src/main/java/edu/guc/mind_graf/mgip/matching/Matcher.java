package edu.guc.mind_graf.mgip.matching;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.guc.mind_graf.cables.Cable;
import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.caseFrames.Adjustability;
import edu.guc.mind_graf.components.Substitutions;
import edu.guc.mind_graf.context.Context;
import edu.guc.mind_graf.exceptions.DirectCycleException;
import edu.guc.mind_graf.exceptions.NoSuchTypeException;
import edu.guc.mind_graf.network.Network;
import edu.guc.mind_graf.network.NetworkController;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.nodes.Syntactic;
import edu.guc.mind_graf.paths.AndPath;
import edu.guc.mind_graf.paths.BUnitPath;
import edu.guc.mind_graf.paths.BangPath;
import edu.guc.mind_graf.paths.ComposePath;
import edu.guc.mind_graf.paths.ConversePath;
import edu.guc.mind_graf.paths.DomainRestrictPath;
import edu.guc.mind_graf.paths.EmptyPath;
import edu.guc.mind_graf.paths.FUnitPath;
import edu.guc.mind_graf.paths.IrreflexiveRestrictPath;
import edu.guc.mind_graf.paths.KPlusPath;
import edu.guc.mind_graf.paths.KStarPath;
import edu.guc.mind_graf.paths.OrPath;
import edu.guc.mind_graf.paths.Path;
import edu.guc.mind_graf.paths.PathTrace;
import edu.guc.mind_graf.paths.RangeRestrictPath;
import edu.guc.mind_graf.relations.Relation;
import edu.guc.mind_graf.set.NodeSet;
import edu.guc.mind_graf.set.PropositionNodeSet;
import edu.guc.mind_graf.support.Pair;
import edu.guc.mind_graf.support.Support;

public class Matcher {
    // List of matches to be returned
    private static ArrayList<Match> matchList;

    // The main matching method (the only one called from outside the class)
    public static List<Match> match(Node queryNode, Context ctx, int attitude) throws DirectCycleException {
        matchList = new ArrayList<>();
        if (queryNode.getSyntacticType() == Syntactic.VARIABLE) {
            for (Node node : Network.getNodes().values()) {
                if (node.equals(queryNode) || !queryNode.getClass().isAssignableFrom(node.getClass()))
                    continue;
                Match match = new Match(new Substitutions(), new Substitutions(), node, 0, new Support(-2));
                if (node.getSyntacticType() == Syntactic.VARIABLE) {
                    match.getFilterSubs().add(node, queryNode);
                } else if (node.getSyntacticType() == Syntactic.MOLECULAR) {
                    if (!occursCheck(queryNode, node, match)) {
                        match.getSwitchSubs().add(queryNode, node);
                    }
                } else
                    match.getSwitchSubs().add(queryNode, node);
                matchList.add(match);
            }
            return matchList;
        }

        if (queryNode.getSyntacticType() == Syntactic.BASE) {
            for (Node node : Network.getNodes().values()) {
                if (node.equals(queryNode) || !queryNode.getClass().isAssignableFrom(node.getClass()))
                    continue;
                if (node.getSyntacticType() == Syntactic.VARIABLE) {
                    Match match = new Match(new Substitutions(), new Substitutions(), node, 0, new Support(-2));
                    match.getFilterSubs().add(node, queryNode);
                    matchList.add(match);
                }
            }
            return matchList;
        }

        for (HashMap<String, Node> molecularSet : Network.getMolecularNodes().values()) {
            for (Node molecular : molecularSet.values()) {
                if (molecular.equals(queryNode))
                    continue;
                Match match = new Match(new Substitutions(), new Substitutions(), molecular, -1, new NodeSet());
                matchList.add(match);
                unify(queryNode, molecular, match, ctx, attitude, 0);
            }
        }

        for (Match match : matchList) {
            if (((NodeSet) match.getSupport()).isEmpty()) {
                match.setSupport(new Support(-2));
            } else {
                PropositionNodeSet propSet = new PropositionNodeSet(((NodeSet) match.getSupport()));
                Pair<PropositionNodeSet, PropositionNodeSet> pair = new Pair<>(propSet, new PropositionNodeSet());
                HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>> map = new HashMap<>();
                map.put(attitude, pair);
                match.setSupport(new Support(-2, attitude, Network.currentLevel, map, new PropositionNodeSet()));
            }
            if (match.getMatchType() == -1) {
                match.setMatchType(0);
            }
        }
        return matchList;
    }

    // Main unification method
    private static boolean unify(Node queryNode, Node node, Match match, Context ctx, int attitude, int scope) {
        if (!queryNode.getClass().isAssignableFrom(node.getClass())) {
            matchList.remove(match);
            return false;
        }
        if (queryNode.getSyntacticType() == Syntactic.BASE && node.getSyntacticType() == Syntactic.BASE) {
            if (!queryNode.getName().equals(node.getName())) {
                matchList.remove(match);
                return false;
            }
        } else if (queryNode.getSyntacticType() == Syntactic.VARIABLE
                || node.getSyntacticType() == Syntactic.VARIABLE) {
            return unifyVariable(queryNode, node, match, ctx, attitude, scope);
        } else if (queryNode.getSyntacticType() == Syntactic.MOLECULAR
                && node.getSyntacticType() == Syntactic.MOLECULAR) {
            pathBasedInference(queryNode, node, match, ctx, attitude, scope);
            if (!matchList.contains(match)) {
                matchList.add(match);
            }
            Object[] downRelationList = queryNode.getDownCableSet().keySet();
            List<Match> molecularMatchList = new ArrayList<>();
            molecularMatchList.add(match);
            boolean nullCables = true;
            for (Object downRelation : downRelationList) {
                List<Match> tempMatchList = new ArrayList<>();
                Cable cable = node.getDownCableSet().get((String) downRelation);
                if (cable == null) {
                    if (checkCable(queryNode, node, Network.getRelations().get(downRelation), match, true)) {
                        continue;
                    } else {
                        matchList.remove(match);
                        return false;
                    }
                }
                nullCables = false;
                List<List<Node>> nodePermutations = getAllPermutations(cable.getNodeSet().getValues());
                for (List<Node> nodePermutation : nodePermutations) {
                    cable = queryNode.getDownCableSet().get((String) downRelation);
                    if (cable == null) {
                        if (checkCable(queryNode, node, Network.getRelations().get(downRelation), match, false)) {
                            continue;
                        } else {
                            matchList.remove(match);
                            return false;
                        }
                    }
                    List<List<Node>> queryNodePermutations = getAllPermutations(
                            cable.getNodeSet().getValues());
                    for (List<Node> queryNodePermutation : queryNodePermutations) {
                        for (Match molecularMatch : molecularMatchList) {
                            Match tempMatch = molecularMatch.clone();
                            if (unifyMolecular(
                                    queryNodePermutation,
                                    nodePermutation,
                                    Network.getRelations().get(downRelation),
                                    tempMatch, ctx, attitude, scope) != null)
                                tempMatchList.add(tempMatch);
                        }
                    }
                }
                if (tempMatchList.isEmpty()) {
                    matchList.remove(match);
                    return false;
                }

                molecularMatchList = new ArrayList<>(removeDuplicates(tempMatchList));
            }
            if (!nullCables) {
                if (!queryNode.isOpen() && !node.isOpen()) {
                    matchList.remove(match);
                    return true;
                }

                matchList.addAll(molecularMatchList);
            }
            matchList.remove(match);
        }
        return true;
    }

    // Unification of two variables
    private static boolean unifyVariable(Node queryNode, Node node, Match match, Context ctx, int attitude, int scope) {
        if ((queryNode.getSyntacticType() == Syntactic.MOLECULAR
                && occursCheck(node, queryNode, match))
                || (node.getSyntacticType() == Syntactic.MOLECULAR && occursCheck(queryNode, node, match))) {
            matchList.remove(match);
            return false;
        }
        /*
         * scope = 0, first one is from the query and the second from the network
         * scope = -1, both are from the network
         * scope = 1, both are from the query
         */
        if (queryNode.getSyntacticType() == Syntactic.VARIABLE && scope != -1) {
            if (match.getSwitchSubs().getMap().get(queryNode) != null) {
                if (NetworkController.isUvbrEnabled()
                        && !match.getSwitchSubs().get(queryNode).equals(node))
                    return false;
                if (scope == 0) {
                    return unify(match.getSwitchSubs().get(queryNode), node, match, ctx, attitude, -1);
                } else { // 1
                    return unify(node, match.getSwitchSubs().get(queryNode), match, ctx, attitude, 0);
                }
            }
        }
        if (node.getSyntacticType() == Syntactic.VARIABLE && scope != 1) {
            if (match.getFilterSubs().getMap().get(node) != null) {
                if (NetworkController.isUvbrEnabled()
                        && !match.getSwitchSubs().get(node).equals(queryNode))
                    return false;
                if (scope == 0) {
                    return unify(queryNode, match.getFilterSubs().get(node), match, ctx, attitude, 1);
                } else { // -1
                    return unify(match.getFilterSubs().get(node), queryNode, match, ctx, attitude, 0);
                }
            }
        }

        if (queryNode.getSyntacticType() == Syntactic.VARIABLE) {
            if (NetworkController.isUvbrEnabled()) {
                if (node.getSyntacticType() == Syntactic.MOLECULAR && node.isOpen()) {
                    return false;
                }
            }
            if (!NetworkController.isUvbrEnabled()
                    || (NetworkController.isUvbrEnabled() && !uvbrTrap(queryNode, node, match.getSwitchSubs()))) {
                if (scope != -1)
                    match.getSwitchSubs().add(queryNode, node);
                else
                    match.getFilterSubs().add(queryNode, node);
                if (!NetworkController.isUvbrEnabled() || (node.getSyntacticType() != Syntactic.VARIABLE
                        && node.getSyntacticType() == Syntactic.MOLECULAR && !node.isOpen()))
                    changeBindingInSubs(queryNode, node, true, match);
            } else {
                matchList.remove(match);
                return false;
            }
        } else {
            if (NetworkController.isUvbrEnabled()) {
                if (queryNode.getSyntacticType() == Syntactic.MOLECULAR && queryNode.isOpen()) {
                    return false;
                }
            }
            if (!NetworkController.isUvbrEnabled()
                    || (NetworkController.isUvbrEnabled() && !uvbrTrap(node, queryNode, match.getFilterSubs()))) {
                if (scope != 1)
                    match.getFilterSubs().add(node, queryNode);
                else
                    match.getSwitchSubs().add(node, queryNode);
                if (!NetworkController.isUvbrEnabled() || (queryNode.getSyntacticType() != Syntactic.VARIABLE
                        && queryNode.getSyntacticType() == Syntactic.MOLECULAR && !queryNode.isOpen()))
                    changeBindingInSubs(node, queryNode, false, match);
            } else {
                matchList.remove(match);
                return false;
            }
        }
        return true;
    }

    // Unification of a permutation of the two cables of two molecular nodes
    private static Match unifyMolecular(List<Node> queryNodeList, List<Node> nodeList, Relation relation,
            Match match, Context ctx, int attitude, int scope) {
        if (queryNodeList.size() != nodeList.size()) {
            // wire based inference
            if (relation.getAdjust() == Adjustability.NONE)
                return null;
            int cableSize = queryNodeList.size();
            int cSize = nodeList.size();
            if (relation.getAdjust() == Adjustability.EXPAND) {
                if (cableSize < cSize) {
                    if (match.getMatchType() == 1)
                        return null;
                    match.setMatchType(2);
                } else
                    match.setMatchType(1);
            }
            if (relation.getAdjust() == Adjustability.REDUCE) {
                if (cableSize > cSize) {
                    if (match.getMatchType() == 1)
                        return null;
                    match.setMatchType(2);
                } else
                    match.setMatchType(1);
            }
        }
        List<Node> nonUnifiedNodes = new ArrayList<Node>(nodeList);
        for (Node queryNode : queryNodeList) {
            boolean isUnified = false;
            if (!nonUnifiedNodes.isEmpty()) {
                Node n = nonUnifiedNodes.iterator().next();
                nonUnifiedNodes.remove(n);
                if (unify(queryNode, n, match, ctx, attitude, scope)) {
                    isUnified = true;
                }
            }
            if (!isUnified) {
                if (relation.getAdjust() == Adjustability.NONE)
                    return null;
                else if (relation.getAdjust() == Adjustability.EXPAND) {
                    if (match.getMatchType() == 2)
                        return null;
                    else {
                        match.setMatchType(1);
                        if (queryNode.getSyntacticType() == Syntactic.VARIABLE)
                            match.getSwitchSubs().add(queryNode, null);
                    }
                } else if (relation.getAdjust() == Adjustability.REDUCE) {
                    if (match.getMatchType() == 1)
                        return null;
                    else {
                        match.setMatchType(2);
                        if (queryNode.getSyntacticType() == Syntactic.VARIABLE)
                            match.getSwitchSubs().add(queryNode, null);
                    }
                } else if (queryNode.getSyntacticType() == Syntactic.VARIABLE) {
                    match.getSwitchSubs().add(queryNode, null);
                }
            }
        }
        for (Node n : nonUnifiedNodes) {
            if (relation.getAdjust() == Adjustability.NONE)
                return null;
            else if (relation.getAdjust() == Adjustability.EXPAND) {
                if (match.getMatchType() == 1)
                    return null;
                else
                    match.setMatchType(2);
            } else if (relation.getAdjust() == Adjustability.REDUCE) {
                if (match.getMatchType() == 2)
                    return null;
                else
                    match.setMatchType(1);
            } else if (n.getSyntacticType() == Syntactic.VARIABLE) {
                match.getFilterSubs().add(n, null);
            }
        }
        return match;
    }

    private static void pathBasedInference(Node queryNode, Node node, Match match, Context ctx, int attitude,
            int scope) {
        List<Match> molecularMatchList = new ArrayList<>();
        molecularMatchList.add(match);
        boolean nullCables = true;
        for (Cable downCable : queryNode.getDownCableSet().getValues()) {
            List<Match> tempMatchList = new ArrayList<>();
            Relation relation = downCable.getRelation();
            Path path = relation.getPath();
            if (downCable == null)
                continue;
            nullCables = false;
            if (path != null && path.passFirstCheck(node, match)) {
                LinkedList<Object[]> listOfNodeList = path.follow(node, new PathTrace(), ctx, attitude);
                if (listOfNodeList == null || listOfNodeList.isEmpty()) {
                    continue;
                } else {
                    Collection<Node> coll = new ArrayList<>();
                    NodeSet support = new NodeSet();
                    for (int i = 0; i < listOfNodeList.size(); i++) {
                        Object[] arr = listOfNodeList.get(i);
                        ((PathTrace) arr[1]).getSupports().addAllTo(support);
                        if (arr[0] != node) {
                            coll.add(((Node) arr[0]));
                        }
                    }
                    List<List<Node>> nodePermutations = getAllPermutations(coll);
                    for (List<Node> nodePermutation : nodePermutations) {
                        List<List<Node>> queryNodePermutations = getAllPermutations(
                                queryNode.getDownCableSet().get(relation.getName()).getNodeSet().getValues());
                        for (List<Node> queryNodePermutation : queryNodePermutations) {
                            for (Match molecularMatch : molecularMatchList) {
                                Match tempMatch = molecularMatch.clone();
                                if (unifyMolecular(
                                        queryNodePermutation,
                                        nodePermutation,
                                        relation,
                                        tempMatch, ctx, attitude, scope) != null) {
                                    support.addAllTo((NodeSet) tempMatch.getSupport());
                                    tempMatchList.add(tempMatch);
                                }
                            }
                        }
                    }
                }
            }
            molecularMatchList.addAll(new ArrayList<>(removeDuplicates(tempMatchList)));
        }
        if (!nullCables) {
            matchList.addAll(molecularMatchList);
        }
        matchList.remove(match);
    }

    // Helper method for changing the bindings in the substitutions to perform
    // composite substitutions
    private static Node changeBindingHelper(Node node, Node nodeToBeChanged, Node nodeChangedTo) {
        if (node.equals(nodeToBeChanged)) {
            return nodeChangedTo;
        }
        if (node.getSyntacticType() == Syntactic.MOLECULAR) {
            DownCableSet dcs = node.getDownCableSet().clone();
            for (Cable cable : dcs.getValues()) {
                NodeSet ns = cable.getNodeSet();
                for (Node n : ns.getValues()) {
                    Node result = changeBindingHelper(n, nodeToBeChanged, nodeChangedTo);
                    if (result != null) {
                        cable.getNodeSet().remove(n);
                        cable.getNodeSet().add(result);
                    }
                }
            }
            try {
                return Network.createNode(node.getName(), dcs);
            } catch (NoSuchTypeException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return null;
    }

    // Method for changing the bindings in the substitutions to perform composite
    // substitutions
    private static void changeBindingInSubs(Node node1, Node node2, boolean checkFilter, Match match) {
        List<Node[]> bindingsToBeChanged = new ArrayList<>();
        Map<Node, Node> map;
        if (checkFilter) {
            map = match.getFilterSubs().getMap();
        } else {
            map = match.getSwitchSubs().getMap();
        }
        for (Node key : map.keySet()) {
            Node value = map.get(key);
            Node result = changeBindingHelper(value, node1, node2);
            if (result != null) {
                map.put(key, result);
                Node[] arr = new Node[2];
                arr[0] = key;
                arr[1] = result;
                if (!NetworkController.isUvbrEnabled() || !result.isOpen())
                    bindingsToBeChanged.add(arr);
            }
        }
        for (Node[] arr : bindingsToBeChanged) {
            changeBindingInSubs(arr[0], arr[1], !checkFilter, match);
        }
    }

    private static List<Match> removeDuplicates(List<Match> list) {
        List<Match> newList = new ArrayList<>(list);
        for (int i = 0; i < newList.size(); i++) {
            for (int j = i + 1; j < newList.size(); j++) {
                if (newList.get(i).isDuplicate(newList.get(j))) {
                    newList.remove(j);
                    j--;
                }
            }
        }
        return newList;
    }

    // Helper method for checking if a UVBR trap is present
    private static boolean uvbrTrap(Node term, Node value, Substitutions subs) {
        NodeSet parents = term.getDirectParents();
        for (Node parent : parents.getValues()) {
            for (Cable cable : parent.getDownCableSet().getValues()) {
                boolean foundOneInstance = false;
                for (Node node : cable.getNodeSet().getValues()) {
                    if (node.getSyntacticType() == Syntactic.VARIABLE) {
                        if (!foundOneInstance && node == term) {
                            foundOneInstance = true;
                            continue;
                        }
                        if (node == term
                                || substitute(node, subs).getName().equals(value.getName())) {
                            return true;
                        }
                    }
                    if (node.getSyntacticType() == value.getSyntacticType()
                            && node.getName().equals(value.getName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static Node substitute(Node node, Substitutions subs) {
        for (Node n : subs.getMap().keySet()) {
            if (n.equals(node))
                return subs.get(n);
        }
        return node;
    }

    private static List<List<Node>> getAllPermutations(Collection<Node> nodesCollection) {
        List<Node> nodesList = new ArrayList<>(nodesCollection);
        List<List<Node>> permutations = new ArrayList<>();

        if (nodesList.size() == 1) {
            permutations.add(new ArrayList<>(nodesList));
            return permutations;
        }

        for (int i = 0; i < nodesList.size(); i++) {
            Node current = nodesList.remove(i);

            List<List<Node>> remaining = getAllPermutations(nodesList);

            for (List<Node> remainingPermutation : remaining) {
                remainingPermutation.add(0, current);
                permutations.add(remainingPermutation);
            }

            nodesList.add(i, current);
        }

        return permutations;
    }

    // Helper method for checking if a variable occurs in a molecular node
    private static boolean occursCheck(Node var, Node node, Match match) {
        if (var.equals(node))
            return true;
        if (node.getSyntacticType() == Syntactic.MOLECULAR) {
            for (Cable cable : node.getDownCableSet().getValues()) {
                for (Node n : cable.getNodeSet().getValues()) {
                    if (n.getSyntacticType() != Syntactic.BASE) {
                        if (occursCheck(var, n, match)) {
                            return true;
                        }
                    }
                }
            }
        } else {
            if (node.getSyntacticType() == Syntactic.VARIABLE) {
                if (match.getFilterSubs().getMap().get(node) != null) {
                    return occursCheck(var, match.getFilterSubs().getMap().get(node), match);
                }
                if (match.getSwitchSubs().getMap().get(node) != null) {
                    return occursCheck(var, match.getSwitchSubs().getMap().get(node), match);
                }
            }
        }
        return false;
    }

    // Helper method for checking if a cable can be null (we check the match type of
    // the current match object and the adjustability of the cable)
    private static boolean checkCable(Node queryNode, Node node, Relation downRelation, Match match,
            boolean isNullInNode) {
        if (downRelation.getAdjust() == Adjustability.NONE)
            return false;

        if (downRelation.getAdjust() == Adjustability.EXPAND) {
            if (isNullInNode) {
                if (match.getMatchType() == 2)
                    return false;
                match.setMatchType(1);
            } else {
                if (match.getMatchType() == 1)
                    return false;
                match.setMatchType(2);
            }
        } else if (downRelation.getAdjust() == Adjustability.REDUCE) {
            if (isNullInNode) {
                if (match.getMatchType() == 1)
                    return false;
                match.setMatchType(2);
            } else {
                if (match.getMatchType() == 2)
                    return false;
                match.setMatchType(1);
            }
        }

        return true;
    }
}
