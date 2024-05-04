package edu.guc.mind_graf.mgip.matching;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.guc.mind_graf.cables.Cable;
import edu.guc.mind_graf.caseFrames.Adjustability;
import edu.guc.mind_graf.components.Substitutions;
import edu.guc.mind_graf.context.Context;
import edu.guc.mind_graf.network.Network;
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

/*
TODO:

Test BangPath

change the uvbr constant to uvbr in Network

replace NodeSet with Support

*/

public class Matcher {
    static ArrayList<Match> matchList;
    static boolean uvbr = true;

    public static List<Match> match(Node queryNode, Context ctx, int attitude) {
        matchList = new ArrayList<>();
        if (queryNode.getSyntacticType() == Syntactic.VARIABLE) {
            for (Node node : Network.getNodes().values()) {
                if (node.equals(queryNode) || !queryNode.getClass().isAssignableFrom(node.getClass()))
                    continue;
                Match match = new Match(new Substitutions(), new Substitutions(), node, 0, new NodeSet()); // replace
                                                                                                           // NodeSet
                                                                                                           // with
                                                                                                           // Support
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
                    Match match = new Match(new Substitutions(), new Substitutions(), node, 0, new NodeSet()); // replace
                                                                                                               // NodeSet
                                                                                                               // with
                                                                                                               // Support
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
                Match match = new Match(new Substitutions(), new Substitutions(), molecular, -1, new NodeSet()); // replace
                                                                                                                 // NodeSet
                                                                                                                 // with
                                                                                                                 // Support
                matchList.add(match);
                unify(queryNode, molecular, match, ctx, attitude);
            }
        }

        for (Match match : matchList) {
            // match.setSupport(new Support(match.getSupport(), attitude)); // replace
            // NodeSet with Support
            if (match.getMatchType() == -1) {
                match.setMatchType(0);
            }
        }
        return matchList;
    }

    private static boolean unify(Node queryNode, Node node, Match match, Context ctx, int attitude) {
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
            return unifyVariable(queryNode, node, match, ctx, attitude);
        } else if (queryNode.getSyntacticType() == Syntactic.MOLECULAR
                && node.getSyntacticType() == Syntactic.MOLECULAR) {
            pathBasedInference(queryNode, node, match.clone(), ctx, attitude);
            Object[] downRelationList = queryNode.getDownCableSet().keySet();
            List<Match> molecularMatchList = new ArrayList<>();
            molecularMatchList.add(match);
            boolean nullCables = true;
            for (Object downRelation : downRelationList) {
                List<Match> tempMatchList = new ArrayList<>();
                Cable cable = node.getDownCableSet().get((String) downRelation);
                if (cable == null)
                    continue;
                nullCables = false;
                List<List<Node>> nodePermutations = getAllPermutations(cable.getNodeSet().getValues());
                for (List<Node> nodePermutation : nodePermutations) {
                    List<List<Node>> queryNodePermutations = getAllPermutations(
                            queryNode.getDownCableSet().get((String) downRelation).getNodeSet().getValues());
                    for (List<Node> queryNodePermutation : queryNodePermutations) {
                        for (Match molecularMatch : molecularMatchList) {
                            Match tempMatch = molecularMatch.clone();
                            if (unifyMolecular(
                                    queryNodePermutation,
                                    nodePermutation,
                                    Network.getRelations().get(downRelation),
                                    tempMatch, ctx, attitude) != null)
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
                matchList.addAll(molecularMatchList);
            }
            matchList.remove(match);
        }
        return true;
    }

    private static boolean unifyVariable(Node queryNode, Node node, Match match, Context ctx, int attitude) {
        if ((queryNode.getSyntacticType() == Syntactic.MOLECULAR
                && occursCheck(node, queryNode, match))
                || (node.getSyntacticType() == Syntactic.MOLECULAR && occursCheck(queryNode, node, match))) {
            matchList.remove(match);
            return false;
        }

        if (queryNode.getSyntacticType() == Syntactic.VARIABLE) {
            if (match.getSwitchSubs().getMap().get(queryNode) != null)
                return unify(match.getSwitchSubs().get(queryNode), node, match, ctx, attitude);
        }
        if (node.getSyntacticType() == Syntactic.VARIABLE) {
            if (match.getFilterSubs().getMap().get(node) != null) {
                return unify(queryNode, match.getFilterSubs().get(node), match, ctx, attitude);
            }
        }

        if (queryNode.getSyntacticType() == Syntactic.VARIABLE)
            if (!uvbr || (uvbr && !uvbrTrap(queryNode, node, match.getSwitchSubs())))
                match.getSwitchSubs().add(queryNode, node);
            else {
                matchList.remove(match);
                return false;
            }
        else if (!uvbr || (uvbr && !uvbrTrap(node, queryNode, match.getFilterSubs())))
            match.getFilterSubs().add(node, queryNode);
        else {
            matchList.remove(match);
            return false;
        }
        return true;
    }

    private static Match unifyMolecular(List<Node> queryNodeList, List<Node> nodeList, Relation relation,
            Match match, Context ctx, int attitude) {
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
                if (unify(queryNode, n, match, ctx, attitude)) {
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

    private static void pathBasedInference(Node queryNode, Node node, Match match, Context ctx, int attitude) {
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
            if (path != null && passPathFirstCheck(node, match, path)) {
                LinkedList<Object[]> listOfNodeList = path.follow(node, new PathTrace(), ctx, attitude);
                if (listOfNodeList == null || listOfNodeList.isEmpty()) {
                    continue;
                } else {
                    Collection<Node> coll = new ArrayList<>();
                    Map<Node, NodeSet> nodeSupportMap = new HashMap<>();
                    for (int i = 0; i < listOfNodeList.size(); i++) {
                        Object[] arr = listOfNodeList.get(i);
                        if (arr[0] != node) {
                            coll.add(((Node) arr[0]));
                            nodeSupportMap.put(((Node) arr[0]), ((PathTrace) arr[1]).getSupports());
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
                                        tempMatch, ctx, attitude) != null)
                                    tempMatchList.add(tempMatch);
                                // add supports
                            }
                        }
                    }
                }
            }
            molecularMatchList = new ArrayList<>(removeDuplicates(tempMatchList));
        }
        if (!nullCables) {
            matchList.addAll(molecularMatchList);
        }
        matchList.remove(match);
    }

    private static boolean passPathFirstCheck(Node node, Match match, Path path) {
        if (path instanceof EmptyPath || path instanceof BangPath || path instanceof KStarPath) {
            return true;
        } else if (path instanceof FUnitPath) {
            return node.getDownCable(((FUnitPath) path).getRelation().getName()) != null;
        } else if (path instanceof BUnitPath) {
            return node.getUpCable(((BUnitPath) path).getRelation().getName()) != null;
        } else if (path instanceof AndPath) {
            for (Path p : ((AndPath) path).getPaths()) {
                if (!passPathFirstCheck(node, match, p)) {
                    return false;
                }
            }
        } else if (path instanceof OrPath) {
            for (Path p : ((OrPath) path).getPaths()) {
                if (passPathFirstCheck(node, match, p)) {
                    return true;
                }
            }
            return false;
        } else if (path instanceof ComposePath
                && !passPathFirstCheck(node, match, ((ComposePath) path).getPaths().getFirst())) {
            return false;
        } else if (path instanceof ConversePath) {
            return passPathFirstCheck(node, match, ((ConversePath) path).getPath().converse());
        } else if (path instanceof IrreflexiveRestrictPath) {
            return passPathFirstCheck(node, match, ((IrreflexiveRestrictPath) path).getPath());
        } else if (path instanceof DomainRestrictPath) {
            return passPathFirstCheck(node, match, ((DomainRestrictPath) path).getP())
                    && passPathFirstCheck(node, match, ((DomainRestrictPath) path).getQ());
        } else if (path instanceof RangeRestrictPath) {
            return passPathFirstCheck(node, match, ((RangeRestrictPath) path).getP());
        } else if (path instanceof KPlusPath) {
            return passPathFirstCheck(node, match, ((KPlusPath) path).getPath());
        }
        return true;
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
}
