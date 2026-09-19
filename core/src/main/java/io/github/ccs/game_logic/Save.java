package io.github.ccs.game_logic;

import java.util.LinkedList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

public final class Save extends Position {
    Board pos;
  LinkedList data = new LinkedList();  
  Preferences pref = Gdx.app.getPreferences("Position");

    public int Saving(Board pos){
        this.pos = pos;
        
        data.add(pos);
    }

       


}
