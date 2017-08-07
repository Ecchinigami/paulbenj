import java.util.*;
import java.io.*;
import java.math.*;

/**
 * Auto-generated code below aims at helping you parse the standard input according to the problem statement.
 **/
class Player {

	public static final String TROOP = "TROOP";
	public static final String BOMB = "BOMB";
	public static final String FACTORY = "FACTORY";
	public static final String MOVE = "MOVE";
	public static final String SPACE = " ";

	public static final int NBBOMB = 2;

	public static int nbFactory;
	public static int nbLinks;

	Map<Integer, Factory> factories;
	Map<Integer, Troop> troops;
	Map<Integer, Bomb> bombs;
	List<LinkFactory> linksFactory;

	EnnemyEntity ennemyEntity = new EnnemyEntity();
	AllyEntity allyEntity = new AllyEntity();
	NeutralEntity neutralEntity = new NeutralEntity();

	static String order = "";

	public Bomb getBombById(int id) {
		return bombs.get(id);
	}

	public Troop getTroopById(int id) {
		return troops.get(id);
	}

	public int getDistanceFactories(Factory facto1, Factory facto2) {
		if (facto1.id == facto2.id) {
			return 0;
		}
		for (LinkFactory linksFactory : linksFactory) {
			if ((linksFactory.idFactory1 == facto1.id && linksFactory.idFactory2 == facto2.id)
					|| (linksFactory.idFactory2 == facto1.id && linksFactory.idFactory1 == facto2.id)) {
				return linksFactory.distance;
			}
		}
		return -1;
	}

	public Player() {

	}

	public void resetRound() {
		order = "";
		troops.clear();
		bombs.clear();
		allyEntity.clear();
		ennemyEntity.clear();
		neutralEntity.clear();

	}

	public Player(int factoryCount, int linkCount) {

		factories = new HashMap(factoryCount);
		troops = new HashMap();
		bombs = new HashMap();
		nbLinks = linkCount;
		linksFactory = new ArrayList<Player.LinkFactory>(linkCount);
		ennemyEntity = new EnnemyEntity();
		allyEntity = new AllyEntity();
		neutralEntity = new NeutralEntity();

	}

	public static void main(String args[]) {

		Scanner in = new Scanner(System.in);
		int factoryCount = in.nextInt(); // the number of factories
		int linkCount = in.nextInt(); // the number of links between factories

		Player player = new Player(factoryCount, linkCount);

		for (int i = 0; i < linkCount; i++) {

			int idFactory1 = in.nextInt();

			if (!player.factories.containsKey(idFactory1)) {
				Factory f1 = player.new Factory(idFactory1);
				player.factories.put(idFactory1, f1);

			}
			int idFactory2 = in.nextInt();
			if (!player.factories.containsKey(idFactory2)) {
				Factory f2 = player.new Factory(idFactory2);
				player.factories.put(idFactory2, f2);
			}

			int distance = in.nextInt();
			LinkFactory linkFacto = player.new LinkFactory(idFactory1, idFactory2, distance);
			player.linksFactory.add(linkFacto);

		}

		player.initDistanceNeighbourFactories();

		// game loop
		while (true) {
			int entityCount = in.nextInt(); // the number of entities (e.g.
											// factories and troops)
			player.resetRound();
			for (int i = 0; i < entityCount; i++) {

				int entityId = in.nextInt();
				String entityType = in.next();
				int arg1 = in.nextInt();
				int arg2 = in.nextInt();
				int arg3 = in.nextInt();
				int arg4 = in.nextInt();
				int arg5 = in.nextInt();

				// Recreation des troops a chaque tour
				if (entityType.equals(TROOP)) {
					Troop troop = player.new Troop(entityId, arg1, arg2, arg3, arg4, arg5);
					player.troops.put(entityId, troop);
				}
				// Maj des facto
				if (entityType.equals(FACTORY)) {
					Factory f = player.factories.get(entityId);
					f.production = arg3;
					f.nbCyborg = arg2;
					if (arg1 == 1) {
						f.isAlly = true;
					} else if (arg1 == -1) {
						f.isAlly = false;
					} else {
						f.isAlly = null;
					}

				}
				// Recreation des bombs a chaque tour
				if (entityType.equals(BOMB)) {
					Bomb b = player.new Bomb(entityId);
					if (arg1 == 1) {
						b.isAlly = true;
					} else {
						b.isAlly = false;
					}
					b.idSource = arg2;
					b.idTarget = arg3;
					b.round = arg4;
					player.bombs.put(entityId, b);

				}
			}
			player.manageSideEntities();

			player.attackBomb();

			for (Factory factoryAlly : player.allyEntity.factories) {
				Factory toAttack = null;
				int nbCyborg = 0;
				int bestChoice = 0;
				Factory bestFactoToAttack = null;

				// Colonisation des factory neutres en priorite qui a une
				// production
				for (Factory factoryNeutral : player.neutralEntity.factories) {

					
					int currentFactoryValue = computeValue(factoryAlly,factoryNeutral);
					
					if (currentFactoryValue > bestChoice) {
						bestChoice = currentFactoryValue;
						bestFactoToAttack = factoryNeutral;
						nbCyborg = computeCyborgToSend(factoryAlly,factoryNeutral);
					}
				}
				if (bestFactoToAttack == null) {
					for (Factory factoryEnnemy : player.ennemyEntity.factories) {
						if (!player.allyEntity.isBombAttackFactory(factoryEnnemy.id)) {
							int distance = factoryAlly.neighborFacto.get(factoryEnnemy);
							int currentChoice = factoryEnnemy.production * 10 / factoryAlly.neighborFacto.get(factoryEnnemy) * 5;
							if (currentChoice > bestChoice) {
								bestChoice = currentChoice;
								bestFactoToAttack = factoryEnnemy;
								nbCyborg = factoryAlly.nbCyborg / distance + factoryAlly.production*5;
							}
						}
					}
				}

				if (bestFactoToAttack != null) {

					player.addOrder("MOVE " + factoryAlly.id + " " + bestFactoToAttack.id + " " + nbCyborg);

				}
				for (Factory factory : player.ennemyEntity.factories) {
					if (factory.production > 0 && !player.allyEntity.isBombAttackFactory(factory.id)) {
						nbCyborg=1;
						if(! player.ennemyEntity.bombs.isEmpty()){
							nbCyborg = factory.production*5;
						}
						player.addOrder("MOVE " + factoryAlly.id + " " + factory.id + " " + nbCyborg);

					}
				}
			}
			sendOrder();
		}

	}
	
	private static int computeCyborgToSend(Factory factoryAlly, Factory factoryTarget) {
		int nbCyborg=0;
		int distance = factoryAlly.neighborFacto.get(factoryTarget);
		if( factoryTarget.isAlly == null){
			nbCyborg = ((factoryTarget.nbCyborg / distance) + factoryAlly.production*5);
		}
		else if( ! factoryTarget.isAlly ) {
			nbCyborg =factoryAlly.nbCyborg / distance + factoryAlly.production*5;
		}
		
		return nbCyborg;
	}

	private static int computeValue(Factory factoryAlly, Factory factoryTarget){
		int value =0;
		int distance = factoryAlly.neighborFacto.get(factoryTarget);
		if( factoryTarget.isAlly == null){
			value =factoryTarget.production * 10 / factoryAlly.neighborFacto.get(factoryTarget) * 10;
		}
		else if( ! factoryTarget.isAlly ) {
			value =factoryTarget.production * 10 / factoryAlly.neighborFacto.get(factoryTarget) * 10;
		}
		
		return value;
	}

	private static void sendOrder() {
		if (!order.isEmpty()) {
			System.out.println(order);
		} else {
			System.out.println("WAIT");
		}

	}

	public void attackBomb() {
		if (allyEntity.bombs.isEmpty()) {

			Map<Integer, Integer> factoTargetCount = new HashMap<Integer, Integer>();
			Map<Integer, Integer> factoTargetAllyCount = new HashMap<Integer, Integer>();

			for (Troop troop : allyEntity.troops) {
				int idTargetTroop = troop.factoTarget;
				Factory factoryTarget = factories.get(idTargetTroop);
				
				if (factoTargetAllyCount.containsKey(factoryTarget.id)) {
					int valueAlly = factoTargetAllyCount.get(factoryTarget.id);
					factoTargetAllyCount.put(factoryTarget.id, ++valueAlly);
				} else {
					factoTargetAllyCount.put(factoryTarget.id, 1);
				}
			}
			for (Troop troop : ennemyEntity.troops) {
				int idTargetTroop = troop.factoTarget;
				Factory factoryTarget = factories.get(idTargetTroop);
				if (factoryTarget.production != 0 && (factoryTarget.isAlly ==null ||!factoryTarget.isAlly)) {
					if (factoTargetCount.containsKey(factoryTarget.id)) {
						int value = factoTargetCount.get(factoryTarget.id);
						factoTargetCount.put(factoryTarget.id, ++value);
					} else {
						factoTargetCount.put(factoryTarget.id, 1);
					}

				}

			}
			int maxValue = 5;
			Factory bombTarget = null;
			for (Integer idFacto : factoTargetCount.keySet()) {
				int value = factoTargetCount.get(idFacto);
				if (value > maxValue) {
					Integer allyValue = factoTargetAllyCount.get(idFacto);
					if (allyValue == null || allyValue <= 2) {

						maxValue = value;
						bombTarget = factories.get(idFacto);
					}
				}
			}

			if (bombTarget != null) {
				Factory factoryBomb = getFactoryAllyNearEnnemyFactory(bombTarget);
				if (factoryBomb != null) {
					addOrder("BOMB " + factoryBomb.id + " " + bombTarget.id);
				}
			}
		}

	}

	public void addOrder(String orderToAdd) {
		if (!order.isEmpty()) {
			order += ";";
		}
		order += orderToAdd;

	}

	private void manageSideEntities() {

		for (Integer idFacto : factories.keySet()) {
			Factory factory = factories.get(idFacto);
			if (factory.isAlly != null) {
				if (factory.isAlly) {
					allyEntity.factories.add(factory);

				} else {
					ennemyEntity.factories.add(factory);
				}
			} else {
				neutralEntity.factories.add(factory);
			}
		}

		for (Integer idTroop : troops.keySet()) {
			Troop troop = getTroopById(idTroop);
			if (troop.isAlly) {
				allyEntity.troops.add(troop);
			} else {
				ennemyEntity.troops.add(troop);
			}
		}
		for (Integer idBomb : bombs.keySet()) {
			Bomb bomb = getBombById(idBomb);
			if (bomb.isAlly) {
				allyEntity.bombs.add(bomb);
			} else {
				ennemyEntity.bombs.add(bomb);
			}
		}

	}

	public Factory getFactoryAllyNearEnnemyFactory(Factory factory) {
		Factory factoToReturn = null;
		int distance = 20;
		for (Factory f : allyEntity.factories) {
			int currentDistance = f.neighborFacto.get(factory);
			if (distance > currentDistance) {
				factoToReturn = f;
				distance = currentDistance;
			}
		}

		return factoToReturn;
	}

	public void initDistanceNeighbourFactories() {

		for (Integer idFactory : factories.keySet()) {
			Factory factory = factories.get(idFactory);

			for (LinkFactory linkFactory : linksFactory) {
				if (factory.id == linkFactory.idFactory1) {
					factory.neighborFacto.put(factories.get(linkFactory.idFactory2), linkFactory.distance);

				} else if (factory.id == linkFactory.idFactory2) {
					factory.neighborFacto.put(factories.get(linkFactory.idFactory1), linkFactory.distance);
				}
			}
		}
	}

	public class LinkFactory {
		int idFactory1;
		int idFactory2;
		int distance;

		public LinkFactory(int idFactory1, int idFactory2, int distance) {
			this.idFactory1 = idFactory1;
			this.idFactory2 = idFactory2;
			this.distance = distance;
		}
	}

	class Troop extends Entity {

		public int factoSource;
		public int factoTarget;
		public int nbTroops;
		public int round;
		public int factoInte;

		Troop(int id, int ally, int factoSource, int factoTarget, int nbTroops, int nbRound) {
			super(id);
			this.factoSource = factoSource;
			this.factoTarget = factoTarget;
			this.nbTroops = nbTroops;
			this.round = nbRound;
			if (ally == 1) {
				this.isAlly = true;
			} else {
				this.isAlly = false;
			}
		}

	}

	public class Factory extends Entity {

		int nbCyborg = -1000;
		int production = -1000;
		Map<Factory, Integer> neighborFacto = new HashMap<Player.Factory, Integer>();

		// Map<Factory,List<LinkFactory>> shortDistanceFactories =new
		// HashMap<Player.Factory, List<LinkFactory>>();

		public Factory(int id) {
			super(id);
		}

		@Override
		public String toString() {
			return "Factory [nbCyborg=" + nbCyborg + ", production=" + production + ", neighborFacto=" + neighborFacto + ", id=" + id
					+ ", name=" + name + ", isAlly=" + isAlly + "]";
		}

	}

	public class Entity {
		Integer id = null;
		int name;
		Boolean isAlly = null;

		public Entity(int idEntity) {
			this.id = new Integer(idEntity);
		}
	}

	public class Bomb extends Entity {

		int idSource = -1000;
		int idTarget = -1;
		int round = -1;

		public Bomb(int id) {
			super(id);
		}
	}

	public class SideEntity {
		Set<Factory> factories = new HashSet<Player.Factory>();
		Set<Troop> troops = new HashSet<Player.Troop>();;
		Set<Bomb> bombs = new HashSet<Player.Bomb>();;

		int nbBombCurrent = NBBOMB;

		public void clear() {
			factories.clear();
			troops.clear();
			bombs.clear();
		}
	}

	public class EnnemyEntity extends SideEntity {
		public EnnemyEntity() {
			super();
		}
	}

	public class AllyEntity extends SideEntity {
		public AllyEntity() {
			super();
		}

		public boolean isBombAttackFactory(int idFactory) {
			for (Bomb bomb : bombs) {
				if (bomb.idTarget == idFactory) {
					return true;
				}
			}
			return false;
		}

	}

	public class NeutralEntity extends SideEntity {
		public NeutralEntity() {
			super();
		}
	}
}
