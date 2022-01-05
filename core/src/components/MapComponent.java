package components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

public class MapComponent implements Component, Disposable {
	public TiledMap map;
	private int lastBackgroundLayerToRenderIndex = 0;
	private Array<TiledMapTileLayer> tileLayers;
	
	public MapComponent(String mapFile) {
		loadMap(mapFile);
	}
	
	public void loadMap(String mapFile) {
		TmxMapLoader mapLoader = new TmxMapLoader();
		map = mapLoader.load(mapFile);
		tileLayers = map.getLayers().getByType(TiledMapTileLayer.class);
		lastBackgroundLayerToRenderIndex = tileLayers.size;
	}
	
	public void setLastBackgroundLayerToRenderIndex(int index) {
		if (index > tileLayers.size) 
			throw new ArrayIndexOutOfBoundsException();
		
		lastBackgroundLayerToRenderIndex = index;
	}
	
	public int getLastBackgroundLayerToRenderIndex() {
		return lastBackgroundLayerToRenderIndex;
	}
	
	public Array<TiledMapTileLayer> getTileLayers() {
		return tileLayers;
	}
	
	public Vector2 getMapSize() {
		float mapWidth = map.getProperties().get("tilewidth", Integer.class) * map.getProperties().get("width", Integer.class);
		float mapHeight = map.getProperties().get("tileheight", Integer.class) * map.getProperties().get("height", Integer.class);
		return new Vector2(mapWidth, mapHeight);
	}

	@Override
	public void dispose() {
		map.dispose();
	}
}
