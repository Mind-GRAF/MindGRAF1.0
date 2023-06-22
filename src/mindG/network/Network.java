package mindG.network;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import mindG.network.cables.DownCable;
import mindG.network.cables.DownCableSet;
import mindG.network.caseFrames.Adjustability;
import mindG.network.components.CustomClass;
import mindG.network.components.CustomConstructor;
import mindG.network.components.CustomMethod;
import mindG.network.components.MyClassCreator;
import mindG.network.paths.AndPath;
import mindG.network.paths.ComposePath;
import mindG.network.paths.FUnitPath;
import mindG.network.paths.KPlusPath;
import mindG.network.paths.Path;
import mindG.network.paths.PathTrace;
import mindG.network.relations.Relation;
import mindG.mgip.KnownInstance;
import mindG.mgip.KnownInstanceSet;
import mindG.mgip.Report;
import mindG.mgip.matching.Substitutions;
import mindG.network.RuleNode;

public class Network {
    private static HashMap<Integer, Node> nodes;
    private static HashMap<String, HashMap<String, Node>> molecularNodes;
    private static HashMap<String, Node> baseNodes;
    private static HashMap<String, Relation> relations;
    private static HashMap<Integer, Node> propositionNodes;
    public static HashMap<String, String> quantifiers = new HashMap<String, String>();
    public static HashMap<String, CustomClass> userDefinedClasses = new HashMap<String, CustomClass>();
    public static int MolecularCount;

    public Network() {
        nodes = new HashMap<Integer, Node>();
        molecularNodes = new HashMap<String, HashMap<String, Node>>();
        baseNodes = new HashMap<String, Node>();
        propositionNodes = new HashMap<Integer, Node>();
        relations = new HashMap<String, Relation>();
    }

    // first constructor for molecular nodes
    public static Node createNode(String SemanticType, DownCableSet downCableSet) throws NoSuchTypeException {
        if (downCableSet.size() == 0) {
            return null;
        }
        String downCablesKey = downCableSet.getMolecularSetKey();
        String molecularKey = downCableSet.getMolecularNodeKey();
        Node node;

        if (!molecularNodes.containsKey(downCablesKey) ||
                ((molecularNodes.containsKey(downCablesKey) &&
                        !molecularNodes.get(downCablesKey).containsKey(molecularKey)))) {

            switch (SemanticType.toLowerCase()) {
                case "propositionnode":

                    node = new PropositionNode(downCableSet);
                    propositionNodes.put(node.getId(), node);
                    break;

                case "actnode":
                    node = new ActNode(downCableSet);
                    break;
                case "individualnode":
                    node = new IndividualNode(downCableSet);
                    break;
                case "rulenode":
s                    break;
                default:
                    if (userDefinedClasses.containsKey(SemanticType)) {
                        CustomClass customClass = userDefinedClasses.get(SemanticType);
                        Class<?> createdClass = customClass.getNewClass();
                        try {
                            node = (Node) customClass.createInstance(createdClass, downCableSet);
                        } catch (InstantiationException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                            return null;
                        } catch (IllegalAccessException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                            return null;
                        } catch (IllegalArgumentException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                            return null;
                        } catch (InvocationTargetException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                            return null;
                        } catch (NoSuchMethodException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                            return null;
                        } catch (SecurityException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                            return null;
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                            return null;
                        }

                    } else {
                        throw new NoSuchTypeException("No such Semantic Type, do you want to create a new one ?");
                    }
            }

            node.fetchFreeVariables();
            if (node.getMolecularType() == MolecularType.CLOSED) {

                for (HashMap<String, Node> molecularSet : molecularNodes.values()) {

                    for (Node molecular : molecularSet.values()) {
                        String MolecularKey = molecular.getDownCableSet().getMolecularNodeKeyWithoutVars();
                        String newNodeKey = downCableSet.getMolecularNodeKeyWithoutVars();
                        if (MolecularKey.equals(newNodeKey)) {
                            return molecular;
                        }
                    }
                }
            }
            nodes.put(node.getId(), node);
            if (molecularNodes.containsKey(downCablesKey)) {
                molecularNodes.get(downCablesKey).put(downCableSet.getMolecularNodeKey(), node);
            } else {
                HashMap<String, Node> newSet = new HashMap<String, Node>();
                newSet.put(downCableSet.getMolecularNodeKey(), node);
                molecularNodes.put(downCablesKey, newSet);
            }
            MolecularCount++;
            return node;
        }

        return molecularNodes.get(downCablesKey).get(molecularKey);

    }

    // second constructor for base nodes
    public static Node createNode(String name, String SemanticType) throws NoSuchTypeException {
        Node node;
        switch (SemanticType.toLowerCase()) {
            case "propositionnode":
                node = new PropositionNode(name, false);
                propositionNodes.put(node.getId(), node);
                break;
            case "actnode":
                node = new ActNode(name, false);
                break;
            case "individualnode":
                node = new IndividualNode(name, false);
                break;
            case "rulenode":
                node = new RuleNode(name, false);
                break;
            default:
                if (userDefinedClasses.containsKey(SemanticType)) {
                    CustomClass customClass = userDefinedClasses.get(SemanticType);
                    Class<?> createdClass = customClass.getNewClass();
                    try {
                        node = (Node) customClass.createInstance(createdClass, name, false);
                    } catch (InstantiationException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        return null;
                    } catch (IllegalAccessException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        return null;
                    } catch (IllegalArgumentException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        return null;
                    } catch (InvocationTargetException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        return null;
                    } catch (NoSuchMethodException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        return null;
                    } catch (SecurityException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        return null;
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        return null;

                    }

                } else {
                    throw new NoSuchTypeException("No such Semantic Type, do you want to create a new one ?");
                }
        }
        if (node != null) {
            if (nodes.containsKey(node.getId()))
                return nodes.get(node.getId());

            nodes.put(node.getId(), node);
            if (node.isBase())
                baseNodes.put(node.getName(), node);
        }

        return node;
    }

    // third constructor for base nodes
    public static Node createVariableNode(String name, String SemanticType) throws NoSuchTypeException {
        Node node;
        switch (SemanticType.toLowerCase()) {
            case "propositionnode":
                node = new PropositionNode(name, true);
                propositionNodes.put(node.getId(), node);
                break;
            case "actnode":
                node = new ActNode(name, true);
                break;
            case "individualnode":
                node = new IndividualNode(name, true);
                break;
            case "rulenode":
                node = new RuleNode(name, true);
                break;
            default:
                if (userDefinedClasses.containsKey(SemanticType)) {
                    CustomClass customClass = userDefinedClasses.get(SemanticType);
                    Class<?> createdClass = customClass.getNewClass();
                    try {
                        node = (Node) customClass.createInstance(createdClass, name, true);
                    } catch (InstantiationException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        return null;
                    } catch (IllegalAccessException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        return null;
                    } catch (IllegalArgumentException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        return null;
                    } catch (InvocationTargetException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        return null;
                    } catch (NoSuchMethodException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        return null;
                    } catch (SecurityException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        return null;
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        return null;
                    }

                } else {
                    throw new NoSuchTypeException("No such Semantic Type, do you want to create a new one ?");
                }
        }
        if (node != null) {
            if (nodes.containsKey(node.getId()))
                return nodes.get(node.getId());
            nodes.put(node.getId(), node);
        }

        return node;
    }

    public static Relation createRelation(String name, String type, Adjustability adjust, int limit) {
        Relation r = new Relation(name, type, adjust, limit);
        relations.put(r.getName(), r);
        return r;
    }

    public static CustomClass createNewSemanticType(String name, String SuperClass, ArrayList<CustomMethod> methods)
            throws Exception {
        CustomClass c = new CustomClass(name, SuperClass);
        Class<?>[] params = { String.class, Boolean.class };
        String[] arguments = { "name", "isVariable" };
        Class<?>[] params2 = { DownCableSet.class };
        String[] arguments2 = { "downCableSet" };
        CustomConstructor constructor = new CustomConstructor(name, params, arguments);
        CustomConstructor constructor2 = new CustomConstructor(name, params2, arguments2);
        ArrayList<CustomConstructor> constructors = new ArrayList<>();
        constructors.add(constructor);
        constructors.add(constructor2);
        Class<?> Hazem = c.createClass(methods, constructors);
        userDefinedClasses.put(name, c);
        return c;
    }

    public static HashMap<String, Relation> getRelations() {
        return relations;
    }

    public static void setRelations(HashMap<String, Relation> relations) {
        Network.relations = relations;
    }

    public static Node getNodeById(int id) {
        return nodes.get(id);
    }

    public static boolean isAssignable(Node parent, Node child) {
        if ((parent.getClass()).isAssignableFrom(child.getClass()))
            return true;
        return false;

    }

    public static HashMap<Integer, Node> getNodes() {
        return nodes;
    }

    public void setNodes(HashMap<Integer, Node> Nodes) {
        nodes = Nodes;
    }

    public static HashMap<String, HashMap<String, Node>> getMolecularNodes() {
        return molecularNodes;
    }

    public static void setMolecularNodes(HashMap<String, HashMap<String, Node>> MolecularNodes) {
        molecularNodes = MolecularNodes;
    }

    public static HashMap<String, Node> getBaseNodes() {
        return baseNodes;
    }

    public static void setBaseNodes(HashMap<String, Node> BaseNodes) {
        baseNodes = BaseNodes;
    }

    public static HashMap<String, String> getQuantifiers() {
        return quantifiers;
    }

    public static HashMap<Integer, Node> getPropositionNodes() {
        return propositionNodes;
    }

    public static void setQuantifiers(HashMap<String, String> quantifiers) {
        Network.quantifiers = quantifiers;
    }

    public Node find(DownCableSet downCableSet) {
        String setKey = downCableSet.getMolecularSetKey();
        String key = downCableSet.getMolecularNodeKey();
        return molecularNodes.get(setKey).get(key);
    }

    public HashMap<String, Node> findMolecularSet(DownCableSet downCableSet) {
        String setKey = downCableSet.getMolecularSetKey();
        return molecularNodes.get(setKey);
    }

    public void printNodes() {
        String result = "";
        for (Node node : nodes.values()) {
            result += node.toString() + "\n";
        }
        System.out.println(result);
    }

    public static void main(String[] args) throws ClassCastException, NoSuchTypeException {
        Network network = new Network();

        // ======================================================

        // // 1. create the base nodes
        Node nemo = Network.createNode("Nemo", "propositionNode");
        Node clown = Network.createNode("clownFish", "propositionNode");
        Node X = Network.createVariableNode("X", "propositionNode");
        Node aquatic = Network.createNode("aquatic", "propositionNode");

        // 2. create the needed relations
        Relation object = Network.createRelation("object", "",
                Adjustability.EXPAND, 2);
        Relation member = Network.createRelation("member", "",
                Adjustability.EXPAND, 2);
        Relation Class = Network.createRelation("Class", "",
                Adjustability.EXPAND, 2);
        Relation property = Network.createRelation("property", "",
                Adjustability.EXPAND, 2);
        Relation antecedent = Network.createRelation("antecedent", "",
                Adjustability.EXPAND, 2);
        Relation cons = Network.createRelation("consequent", "",
                Adjustability.EXPAND, 2);
        Network.quantifiers.put("forall", "forall");

        Relation forAll = Network.createRelation("forall", "",
                Adjustability.EXPAND, 2);
        // 3. create downcables for each molecularNode

        DownCable d1 = new DownCable(object, new NodeSet(X));
        DownCable d2 = new DownCable(member, new NodeSet(nemo));
        DownCable d5 = new DownCable(member, new NodeSet(X));
        DownCable d3 = new DownCable(Class, new NodeSet(clown));
        DownCable d4 = new DownCable(property, new NodeSet(aquatic));

        // 4.create molecular nodes
        Node M0 = Network.createNode("propositionnode",
                new DownCableSet(d2, d3));

        Node M1 = Network.createNode("propositionnode",
                new DownCableSet(d3, d5));
        Node M2 = Network.createNode("propositionnode",
                new DownCableSet(d1, d4));
        DownCable d6 = new DownCable(antecedent, new NodeSet(M1));
        DownCable d7 = new DownCable(cons, new NodeSet(M2));
        DownCable d8 = new DownCable(forAll, new NodeSet(X));
        Node P3 = Network.createNode("rulenode",
                new DownCableSet(d6, d8, d7));

        network.printNodes();
        ((PropositionNode) M2).deduce();

    }

}
