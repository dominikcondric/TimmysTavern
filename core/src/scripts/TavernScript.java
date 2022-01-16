package scripts;

import java.util.Random;
import java.util.Stack;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.StringBuilder;
import com.gdx.game.TimmysTavern;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;

import components.AddEntityComponent;
import components.DestroyEntityComponent;
import components.EntityBits;
import components.GuiComponent;
import components.PhysicsComponent;
import components.ScriptComponent;
import components.SoundComponent;
import components.SpriteComponent;

public class TavernScript extends Script {
	private Label finishedOrdersCounterLabel;
	private int finishedOrdersCounter = 0;
	private boolean leverInRange = false;
	private Entity randomNPC = null;
	private final int MAX_CUSTOMERS = 8;
	private int activeNPCCount = 0;
	private float timeToNextNPC = 0.f;
	private boolean tavernOpen = false;
	private Random randomNumberGenerator;
	private String[] npcFileNames = { "Generic Children NPCs A.png", "Generic Children NPCs.png", "Generic Female NPCs A.png", "Generic Female NPCs.png",
			"Generic Male NPCs A.png", "Generic Male NPCs.png" };
	private final Vector2[] seatPositions = { new Vector2(2.5f, 2f), new Vector2(2.5f, 4f), new Vector2(5.5f, 2f), new Vector2(5.5f, 4f),
			new Vector2(12.5f, 6.5f), new Vector2(16f, 6.5f), new Vector2(12.5f, 3.5f), new Vector2(16f, 3.5f)};
	private Stack<Integer> availableSeats;
	
	public TavernScript(Entity selfEntity) {
		super(selfEntity);
		randomNumberGenerator = new Random();
		timeToNextNPC = 10.f + randomNumberGenerator.nextFloat() * 20.f;
		availableSeats = new Stack<Integer>();
		for  (int i = 0; i < MAX_CUSTOMERS; ++i)
			availableSeats.push(i);
		
		GuiComponent guiComponent = new GuiComponent();
		Group group1 = new Group();
		Texture imageBorder = new Texture(Gdx.files.internal("InventoryItemBorder.png"));
		Image borderImage = new Image(imageBorder);
		borderImage.setFillParent(true);
		group1.addActor(borderImage);
		
		LabelStyle labelStyle = new LabelStyle(TimmysTavern.font, Color.WHITE);
		Label label = new Label("Pritisni ENTER da otvoris gostionicu", labelStyle);
		label.setFillParent(true);
		label.setFontScale(2.f);
		label.setWrap(true);
		label.setAlignment(Align.center);
		group1.addActor(label);
		group1.setVisible(false);
		
		group1.setPosition(Gdx.graphics.getWidth() / 12.f, Gdx.graphics.getHeight() * 0.8f);
		group1.setSize(Gdx.graphics.getWidth() / 5.f, Gdx.graphics.getHeight() * 0.1f);
		guiComponent.actors.addActor(group1);
		
		Group group2 = new Group();
		Image anotherBorderImage = new Image(imageBorder);
		anotherBorderImage.setFillParent(true);
		group2.addActor(anotherBorderImage);
		
		finishedOrdersCounterLabel = new Label("0", labelStyle);
		finishedOrdersCounterLabel.setFillParent(true);
		finishedOrdersCounterLabel.setAlignment(Align.center);
		finishedOrdersCounterLabel.setFontScale(3.f);
		group2.addActor(finishedOrdersCounterLabel);
		group2.setPosition(Gdx.graphics.getWidth() / 2.f - Gdx.graphics.getWidth() * 0.05f, Gdx.graphics.getHeight() * 0.8f);
		group2.setSize(Gdx.graphics.getWidth() * 0.1f, Gdx.graphics.getHeight() * 0.1f);
		group2.setVisible(false);
		guiComponent.actors.addActor(group2);
		
		guiComponent.actors.addActor(group2);
		
		SoundComponent npcSoundComp = new SoundComponent();
		npcSoundComp.addSound("MealTaken", Gdx.files.internal("RPGsounds_Kenney\\OGG\\beltHandle1.ogg"), false, false);
		self.add(npcSoundComp);
		
		self.add(guiComponent);
	}

	@Override
	public void update(float deltaTime) {
		if (leverInRange && Gdx.input.isKeyJustPressed(Keys.ENTER)) {
			tavernOpen = !tavernOpen;
			if (!tavernOpen) {
				GuiComponent guiComp = self.getComponent(GuiComponent.class);
				Label label = (Label)((Group)guiComp.actors.getChild(0)).getChild(1);
				StringBuilder labelText = label.getText();
				labelText.replace("zatvoris", "otvoris");
				self.getComponent(ScriptComponent.class).eventsToDispatch.add("TavernClosed");
				label.layout();
				guiComp.actors.getChild(1).setVisible(false);
			} else {
				activeNPCCount = 0;
				availableSeats.clear();
				for  (int i = 0; i < MAX_CUSTOMERS; ++i)
					availableSeats.push(i);
				finishedOrdersCounter = 0;
				GuiComponent guiComp = self.getComponent(GuiComponent.class);
				Label label = (Label)((Group)guiComp.actors.getChild(0)).getChild(1);
				StringBuilder labelText = label.getText();
				labelText.replace("otvoris", "zatvoris");
				label.layout();
				((Label)((Group)guiComp.actors.getChild(1)).getChild(1)).setText("0");
				guiComp.actors.getChild(1).setVisible(true);
			}
		}
		
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
		scriptComp.eventsToListen.add("TavernClosed");
		npc.add(scriptComp);
		
		return npc;
	}
	
	@Override
	public void onCollisionBegin(Contact contact, Fixture self, Fixture other) {
		if ((self.getFilterData().categoryBits & EntityBits.INTERACTABLE_B2D_BIT) != 0) {
			GuiComponent guiComp = this.self.getComponent(GuiComponent.class);
			guiComp.actors.getChild(0).setVisible(true);
			leverInRange = true;
		}
	}
	
	@Override
	public void onCollisionEnd(Contact contact, Fixture self, Fixture other) {
		if ((self.getFilterData().categoryBits & EntityBits.INTERACTABLE_B2D_BIT) != 0) {
			GuiComponent guiComp = this.self.getComponent(GuiComponent.class);
			guiComp.actors.getChild(0).setVisible(false);
			leverInRange = false;
		}
	}
	
	@Override
	public void onEventReceived(Entity sender, String eventName) {
		if (eventName.contentEquals("NPCLeft")) {
			--activeNPCCount;
			availableSeats.push(((NPCCustomerScript)sender.getComponent(ScriptComponent.class).script).seatIndex);
		} else if (eventName.contentEquals("NPCMealTake")) {
			finishedOrdersCounter++;
			self.getComponent(SoundComponent.class).getSoundEffect("MealTaken").shouldPlay = true;
			((Label)((Group)self.getComponent(GuiComponent.class).actors.getChild(1)).getChild(1)).setText(Integer.toString(finishedOrdersCounter));
		}
	}

	@Override
	public void onEventResponse(Entity receiver, String eventName) {
		if (eventName == "BorrowWorld") {
			self.getComponent(ScriptComponent.class).eventsToDispatch.remove(eventName);
			World world = receiver.getComponent(PhysicsComponent.class).body.getWorld();
			
			SpriteComponent spriteComp = randomNPC.getComponent(SpriteComponent.class);
			
			BodyDef bodyDef = new BodyDef();
			bodyDef.position.set(spriteComp.position.x + spriteComp.getSpriteSize().x / 2.f, spriteComp.position.y + spriteComp.getSpriteSize().y / 2.f);
			bodyDef.type = BodyType.StaticBody;
			Body body = world.createBody(bodyDef);
			
			PolygonShape polyShape = new PolygonShape();
			polyShape.setAsBox(spriteComp.getSpriteSize().x / 4.f, spriteComp.getSpriteSize().y / 8.f, new Vector2(0.f, -spriteComp.getSpriteSize().y / 3.f), 0.f);
			FixtureDef fixtureDef = new FixtureDef();
			fixtureDef.shape = polyShape;
			fixtureDef.filter.categoryBits = EntityBits.INTERACTABLE_B2D_BIT;
			fixtureDef.filter.maskBits = EntityBits.PLAYER_B2D_BIT;
			body.createFixture(fixtureDef).setUserData(randomNPC.getComponent(ScriptComponent.class).script);
			
			randomNPC.add(new PhysicsComponent(body));
			randomNPC = null;
			activeNPCCount++;
		} else if (eventName == "TavernClosed") {
			self.getComponent(ScriptComponent.class).eventsToDispatch.remove(eventName);
		}
	}

}
