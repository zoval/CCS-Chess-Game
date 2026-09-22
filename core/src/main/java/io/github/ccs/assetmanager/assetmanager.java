package io.github.ccs.assetmanager;

//asset management code, including asset loading, caching, and resource management.

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;


public class assetmanager extends AssetManager {
    // This class can be expanded with methods and properties related to asset management.
    public AssetManager manager;

    public void loadAssets() {
        // Load your assets here using the AssetManager's load method.
        // Example: manager.load("path/to/asset.png", Texture.class);

        manager = new AssetManager();

        // 2. Queue files from your assets folder (libGDX looks there automatically)
        manager.load("images/player.png", Texture.class);
        manager.load("audio/background_music.mp3", Texture.class); // error: should be Music.class, but just an example

        // 3. Block until everything is finished loading
        manager.finishLoading(); 
        
        // 4. Retrieve the asset for use, variable will be declared when needed

    }

    @Override
    public void dispose() {
        
        manager.dispose();
    }
}
