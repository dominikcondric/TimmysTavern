package scripts;

import java.util.Random;

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
	private Entity[] activeNPCs;
	private final int MAX_CUSTOMERS = 8;
	private int activeNPCCount = 0;
	private float timeToNextNPC = 0.f;
	private boolean tavernOpen = true;
	private Random randomNumberGenerator;
	private boolean shouldTakeOrder = false;
	private String[] npcFileNames = { "Generic Children NPCs A.png", "Generic Children NPCs.png", "Generic Female NPCs A.png", "Generic Female NPCs.png",
			"Generic Male NPCs A.png", "Generic Male NPCs.png" };

	public TavernScript(Entity selfEntity) {
		super(selfEntity);
		activeNPCs = new Entity[MAX_CUSTOMERS];
		randomNumberGenerator = new Random();
		timeToNextNPC = 0.f + randomNumberGenerator.nextFloat() * 10.f;
	}

	@Override
	public void update(float deltaTime) {
		if (tavernOpen) {
			if (!shouldTakeOrder) {
				timeToNextNPC -= deltaTime;
				if (timeToNextNPC <= 0.f) {
					Entity randomNPC = generateRandomNPC();
					activeNPCs[activeNPCCount] = randomNPC;
					shouldTakeOrder = true;
					AddEntityComponent addEntityComp = self.getComponent(AddEntityComponent.class); 
					addEntityComp.entitiesToAdd.clear();
					addEntityComp.entitiesToAdd.add(randomNPC);
					addEntityComp.load = true;
					timeToNextNPC = 0.f + randomNumberGenerator.nextFloat() * 10.f;
				}
			} else {
				
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
		
		int startingY = 3 * npcHeight + npcHeight * (randomNpcTextureIndex / 4) * 4;
		int startingX = npcWidth + (randomNpcTextureIndex % 4) * npcWidth * 3;
		
//		Sprite[] sprites = new Sprite[4];
//		for (int i = 0; i < sprites.length; ++i) {
//			sprites[i] = new Sprite(new TextureRegion(new Texture(Gdx.files.internal(npcFileNames[randomNpcTextureIndex])), startingX, startingY + i * 110, npcWidth, npcHeight));
//		}
		
		final float npcWHRatio = (float)npcWidth / npcHeight;
		Sprite sprite = new Sprite(new TextureRegion(new Texture(Gdx.files.internal(npcFileNames[randomFileIndex])), startingX, startingY, npcWidth, npcHeight));
		sprite.setSize(2.5f * npcWHRatio, 2.5f);
		SpriteComponent spriteComp = (SpriteComponent)npc.addAndReturn(new SpriteComponent(sprite));
		spriteComp.position.set(6f, 5.5f);
		
		DestroyEntityComponent destroyEntityComp = (DestroyEntityComponent)npc.addAndReturn(new DestroyEntityComponent());
		destroyEntityComp.destroyOnSceneChange = false;
		destroyEntityComp.disposeResources = true;
		self.getComponent(ScriptComponent.class).eventsToDispatch.add("BorrowWorld");
		
		((ScriptComponent)npc.addAndReturn(new ScriptComponent(new NPCCustomerScript(npc, "Pita od jabuka")))).eventsToListen.add("SceneChanged");
		
		
		return npc;
	}

	@Override
	public void onEventResponse(Entity receiver, String eventName) {
		if (eventName == "BorrowWorld")
		{
			self.getComponent(ScriptComponent.class).eventsToDispatch.remove(eventName);
			World world = receiver.getComponent(PhysicsComponent.class).body.getWorld();
			
			SpriteComponent spriteComp = activeNPCs[activeNPCCount].getComponent(SpriteComponent.class);
			
			BodyDef bodyDef = new BodyDef();
			bodyDef.position.set(spriteComp.position.x + spriteComp.getSpriteSize().x / 2.f, spriteComp.position.y + spriteComp.getSpriteSize().y / 2.f);
			bodyDef.type = BodyType.StaticBody;
			Body body = world.createBody(bodyDef);
			
			PolygonShape polyShape = new PolygonShape();
			polyShape.setAsBox(spriteComp.getSpriteSize().x / 2.f, spriteComp.getSpriteSize().y, new Vector2(0.f, spriteComp.getSpriteSize().y / 2.f), 0.f);
			FixtureDef fixtureDef = new FixtureDef();
			fixtureDef.shape = polyShape;
			fixtureDef.isSensor = true;
			fixtureDef.filter.categoryBits = EntityBits.INTERACTABLE_B2D_BIT;
			fixtureDef.filter.maskBits = EntityBits.PLAYER_B2D_BIT;
			body.createFixture(fixtureDef).setUserData(activeNPCs[activeNPCCount].getComponent(ScriptComponent.class).script);
			
			activeNPCs[activeNPCCount].add(new PhysicsComponent(body));
			activeNPCCount++;
		}
	}

}
