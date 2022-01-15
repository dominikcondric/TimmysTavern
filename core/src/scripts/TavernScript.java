package scripts;

import java.util.Random;
import java.util.Stack;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;

import components.AddEntityComponent;
import components.DestroyEntityComponent;
import components.EntityBits;
import components.PhysicsComponent;
import components.ScriptComponent;
import components.SpriteComponent;

public class TavernScript extends Script {
	private Entity randomNPC = null;
	private final int MAX_CUSTOMERS = 8;
	private int activeNPCCount = 0;
	private float timeToNextNPC = 0.f;
	private boolean tavernOpen = true;
	private Random randomNumberGenerator;
	private String[] npcFileNames = { "Generic Children NPCs A.png", "Generic Children NPCs.png", "Generic Female NPCs A.png", "Generic Female NPCs.png",
			"Generic Male NPCs A.png", "Generic Male NPCs.png" };
	private final Vector2[] seatPositions = { new Vector2(2.5f, 2f), new Vector2(2.5f, 4f), new Vector2(5.5f, 2f), new Vector2(5.5f, 4f),
			new Vector2(12.5f, 7f), new Vector2(16f, 7f), new Vector2(12.5f, 4f), new Vector2(16f, 4f)};
	private Stack<Integer> availableSeats;
	
	public TavernScript(Entity selfEntity) {
		super(selfEntity);
		randomNumberGenerator = new Random();
		timeToNextNPC = 10.f + randomNumberGenerator.nextFloat() * 20.f;
		availableSeats = new Stack<Integer>();
		for  (int i = 0; i < MAX_CUSTOMERS; ++i)
			availableSeats.push(i);
	}

	@Override
	public void update(float deltaTime) {
		if (tavernOpen) {
			if (activeNPCCount < MAX_CUSTOMERS) {
				timeToNextNPC -= deltaTime;
				if (timeToNextNPC <= 0.f) {
					randomNPC = generateRandomNPC();
					AddEntityComponent addEntityComp = self.getComponent(AddEntityComponent.class); 
					addEntityComp.entitiesToAdd.clear();
					addEntityComp.entitiesToAdd.add(randomNPC);
					addEntityComp.load = true;
					timeToNextNPC = 10.f + randomNumberGenerator.nextFloat() * 20.f;
				}
			}
		}
	}
	
	private Entity generateRandomNPC() {
		Entity npc = new Entity();
		
		final int randomFileIndex = randomNumberGenerator.nextInt(6);
		int randomNpcTextureIndex = 0;
		
		switch (randomFileIndex) {
			case 0:
			case 1:
				randomNpcTextureIndex = randomNumberGenerator.nextInt(8);
				break;
			case 2:
			case 3:
			{
				int[] availableIndices = {0, 1, 3, 5, 6, 7};
				randomNpcTextureIndex = availableIndices[randomNumberGenerator.nextInt(availableIndices.length)];
				break;
			}
			case 4:
			case 5:
			{
				int[] availableIndices = {0, 1, 4, 5, 6, 7};
				randomNpcTextureIndex = availableIndices[randomNumberGenerator.nextInt(availableIndices.length)];
				break;
			}
		}
		
		final int npcHeight = 120;
		final int npcWidth = 80;
		
		int seatIndex = availableSeats.pop();
		Vector2 npcPosition = seatPositions[seatIndex];
		
		int startingY = 1 * npcHeight + npcHeight * (randomNpcTextureIndex / 4) * 4;
		int startingX = npcWidth + (randomNpcTextureIndex % 4) * npcWidth * 3;
		
		if (npcPosition.x == 2.5f || npcPosition.x == 12.5f) {
			startingY += npcHeight;
		}
		
		final float npcWHRatio = (float)npcWidth / npcHeight;
		Sprite sprite = new Sprite(new TextureRegion(new Texture(Gdx.files.internal(npcFileNames[randomFileIndex])), startingX, startingY, npcWidth, npcHeight));
		sprite.setSize(2.5f * npcWHRatio, 2.5f);
		SpriteComponent spriteComp = (SpriteComponent)npc.addAndReturn(new SpriteComponent(sprite));
		
		spriteComp.position.set(npcPosition);
		
		DestroyEntityComponent destroyEntityComp = (DestroyEntityComponent)npc.addAndReturn(new DestroyEntityComponent());
		destroyEntityComp.destroyOnSceneChange = false;
		destroyEntityComp.disposeResources = true;
		self.getComponent(ScriptComponent.class).eventsToDispatch.add("BorrowWorld");
		
		ScriptComponent scriptComp = new ScriptComponent(new NPCCustomerScript(npc, seatIndex));
		scriptComp.eventsToListen.add("SceneChanged");
		npc.add(scriptComp);
		
		return npc;
	}
	
	@Override
	public void onEventReceived(Entity sender, String eventName) {
		if (eventName.contentEquals("NPCLeft")) {
			--activeNPCCount;
			availableSeats.push(((NPCCustomerScript)sender.getComponent(ScriptComponent.class).script).seatIndex);
		}
	}

	@Override
	public void onEventResponse(Entity receiver, String eventName) {
		if (eventName == "BorrowWorld")
		{
			self.getComponent(ScriptComponent.class).eventsToDispatch.remove(eventName);
			World world = receiver.getComponent(PhysicsComponent.class).body.getWorld();
			
			SpriteComponent spriteComp = randomNPC.getComponent(SpriteComponent.class);
			
			BodyDef bodyDef = new BodyDef();
			bodyDef.position.set(spriteComp.position.x + spriteComp.getSpriteSize().x / 2.f, spriteComp.position.y + spriteComp.getSpriteSize().y / 8.f);
			bodyDef.type = BodyType.StaticBody;
			Body body = world.createBody(bodyDef);
			
			PolygonShape polyShape = new PolygonShape();
			polyShape.setAsBox(spriteComp.getSpriteSize().x / 4.f, spriteComp.getSpriteSize().y / 8.f, new Vector2(0.f, -spriteComp.getSpriteSize().y / 2.f), 0.f);
			FixtureDef fixtureDef = new FixtureDef();
			fixtureDef.shape = polyShape;
			fixtureDef.filter.categoryBits = EntityBits.INTERACTABLE_B2D_BIT;
			fixtureDef.filter.maskBits = EntityBits.PLAYER_B2D_BIT;
			body.createFixture(fixtureDef).setUserData(randomNPC.getComponent(ScriptComponent.class).script);
			
			randomNPC.add(new PhysicsComponent(body));
			randomNPC = null;
			activeNPCCount++;
		}
	}

}
