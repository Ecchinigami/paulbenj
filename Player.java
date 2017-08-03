import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
class Player {

    public static void main(String args[]) {

        Factories factories = new Factories();

        //Set<Factory> listFactory = new HashSet<Factory>();
        Set<Troop> listTroop = new HashSet<Troop>();

        Scanner in = new Scanner(System.in);
        int factoryCount = in.nextInt(); // the number of factories
        factories.setNbFactory(factoryCount);
        System.err.println("Factory Count : " + factoryCount);
        int linkCount = in.nextInt(); // the number of links between factories
        System.err.println("Link Count : " + factoryCount);
        for (int i = 0; i < linkCount; i++) {
            int factory1 = in.nextInt();
            int factory2 = in.nextInt();
            int distance = in.nextInt();
            System.err.println("Link : " + factory1 + " - " + factory2 + " (" + distance + ")");
            factories.addLink(factory1, factory2, distance);
        }

        // game loop
        while (true) {
            //listFactory.clear();
            listTroop.clear();

            int entityCount = in.nextInt(); // the number of entities (e.g. factories and troops)
            System.err.println("Entity Count : " + entityCount);
            for (int i = 0; i < entityCount; i++) {
                int entityId = in.nextInt();
                String entityType = in.next();
                int arg1 = in.nextInt();
                int arg2 = in.nextInt();
                int arg3 = in.nextInt();
                int arg4 = in.nextInt();
                int arg5 = in.nextInt();
                if ("FACTORY".equals(entityType)) {
                    System.err.println("Entity : id:" + entityId + ", type:" + entityType + ", possession:" + arg1
                            + ", nbCyborg:" + arg2 + ", nbProduction:" + arg3);
                    //listFactory.add(new Factory(entityId, arg1, arg2, arg3));
                    factories.updateFactories(entityId, arg1, arg2, arg3);
                } else {
                    System.err.println("Entity : id:" + entityId + ", type:" + entityType + ", possesion:" + arg1
                            + ", idStartFactory:" + arg2 + ", idEndFactory:" + arg3 + ", nbCyborg:" + arg4
                            + ", nbTurnToArrive:" + arg5);
                    listTroop.add(new Troop(entityId, arg1, arg2, arg3, arg4, arg5));
                }
            }

            // Write an action using System.out.println()
            // To debug: System.err.println("Debug messages...");

            // Any valid action, such as "WAIT" or "MOVE source destination cyborgs"
            System.out.println("WAIT");
        }
    }
}

class Factories {

    int nbFactory;

    Set<Link> listLink = new HashSet<Link>();
    //Map<Integer, Integer> factoriesProduction = new HashMap<Integer, Integer>();
    Map<Integer, Factory> factoriesProperties = new HashMap<Integer, Factory>();

    public Factories() {
    }

    public void setNbFactory(int nbFactory) {
        this.nbFactory = nbFactory;
    }

    public void addLink(int idFactory1, int idFactory2, int distance) {
        listLink.add(new Link(idFactory1, idFactory2, distance));
    }

    /*public void setProduction(int idFactory, int production) {
        if (factoriesProduction.containsKey(idFactory)) {
            factoriesProduction.replace(idFactory, production);
        } else {
            factoriesProduction.put(idFactory, production);
        }
    }*/

    public void updateFactories(int idFactory, int possession, int nbCyborg, int production) {
        if (factoriesProperties.containsKey(idFactory)) {
            factoriesProperties.replace(idFactory, new Factory(idFactory, possession, nbCyborg, production));
        } else {
            factoriesProperties.put(idFactory, new Factory(idFactory, possession, nbCyborg, production));
        }
    }

    public int getBestFactoryToTake(int idFactory) {
        for (Integer currentId : factoriesProperties.keySet()) {
            //factoriesProperties.get(currentId).ge
        }
        return 0;
    }

    public int getDistance(int idFactory1, int idFactory2) {
        for (Link link : listLink) {
            if (link.getIdFactory1() == idFactory1 && link.getIdFactory2() == idFactory2) {
                return link.getDistance();
            } else if (link.getIdFactory2() == idFactory1 && link.getIdFactory1() == idFactory2) {
                return link.getDistance();
            }
        }
        return 0;
    }

    class Link {

        private int idFactory1;
        private int idFactory2;
        private int distance;

        public Link(int idFactory1, int idFactory2, int distance) {
            this.idFactory1 = idFactory1;
            this.idFactory2 = idFactory2;
            this.distance = distance;
        }

        public int getIdFactory1() {
            return idFactory1;
        }

        public int getIdFactory2() {
            return idFactory2;
        }

        public int getDistance() {
            return distance;
        }
    }
}

class Troop {
    public int id;
    public int possession;
    public int factorySource;
    public int factoryTarget;
    public int nbCyborg;
    public int nbRoundToTarget;

    Troop(int id, int possession, int factorySource, int factoryTarget, int nbCyborg, int nbRoundToTarget) {
        this.id = id;
        this.possession = possession;
        this.factorySource = factorySource;
        this.factoryTarget = factoryTarget;
        this.nbCyborg = nbCyborg;
        this.nbRoundToTarget = nbRoundToTarget;
    }
}

class Factory {

    private int id;
    private int possession;
    private int nbCyborg;
    private int production;

    public Factory(int id, int possession, int nbCyborg, int production) {
        this.id = id;
        this.possession = possession;
        this.nbCyborg = nbCyborg;
        this.production = production;
    }

    public int getId() {
        return id;
    }

    public int getPossession() {
        return possession;
    }

    public int getNbCyborg() {
        return nbCyborg;
    }

    public int getProduction() {
        return production;
    }
}
