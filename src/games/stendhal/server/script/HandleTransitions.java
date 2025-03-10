/***************************************************************************
 *                   (C) Copyright 2003-2012 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.script;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.scripting.ScriptImpl;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.fsm.Transition;
import games.stendhal.server.entity.player.Player;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * Handle transitions of the NPC
 * 
 * @author yoriy
 */
public class HandleTransitions extends ScriptImpl {

	private void usage(final Player admin) {
		admin.sendPrivateText("Użyj /script AlterTransitions.class <npc> <-><polecenie> <argumenty>.\n" +
				"Polecenia:\n" +
				"\nadd - dodaje zmianę do npca.\n" +
				"\tArgumenty: \"nazwa\" \"wywołanie\" \"tekst do wypowiedzenia\"\n" +
				"\ndel - usuwa zmainę z npca.\n" +
				"\tArgumenty: \"nazwa\"\n" +
				"\nalter - zmienia zmianę.\n" +
				"\tArgumenty: \"nazwa\" \"wywołanie\" \"tekst do wypowiedzenia\"\n" +
				"\nlist - pokazuje listę aktualnych zmian.\n" +
				"\tArgumenty: nic\n" +
				"");
	}
	
	@Override
	public void execute(final Player admin, final List<String> args) {
		
		Logger.getLogger(this.getClass()).info(
				"admin: "+admin.getName()+", arguments: "+args.toString());
		
		// help text
		if (args.size() < 2) {
			usage(admin);
			return;
		}
		
		// npc
        final String npc_name = args.get(0);
        if(npc_name.equals("")) {
        	admin.sendPrivateText("Nie dozwolona jest pusta nazwa NPCa.");
        	return;
        }
    	SpeakerNPC npc=SingletonRepository.getNPCList().get(npc_name);
    	if(npc==null) {
    		admin.sendPrivateText("brak NPCa ("+npc_name+") w grze.");
    	}
    	
        // check command
        String command = args.get(1);
        if(!command.startsWith("-")) {
        	admin.sendPrivateText("polecenie powinno się zaczynać od \"-\"");
        	return;
        } else {
        	command = command.substring(1);
        }
        
        // fill arguments
        List<String> arguments = new LinkedList<String>();
        for(int i=2; i<args.size(); i++) {
        	arguments.add(args.get(i));
        }
        
        // parse command
        if(command.equals("add")) {
        	if(arguments.size()<3) {
        		usage(admin);
        		return;
        	}
        	final String label=arguments.get(0);
        	// TODO: split triggers
        	final String trigger=arguments.get(1);
        	final String text=arguments.get(2);
        	if(label.equals("")) {
        		admin.sendPrivateText("nazwa nie powinna być pusta dla "+command);
        		return;
        	}
        	npc.add(ConversationStates.ANY, trigger, null, ConversationStates.ANY, text, null, label);
        	admin.sendPrivateText("dodano zmainę.");
        	return;
        	
        } else if(command.equals("del")) {
        	if(arguments.size()<1) {
        		usage(admin);
        		return;
        	}
        	final String label=arguments.get(0);
        	if(label.equals("")) {
        		admin.sendPrivateText("nazwa nie powinna być pusta dla "+command);
        		return;
        	} 
        	if(npc.del(label)) {
        		admin.sendPrivateText("zmiana została usunięta.");
        	} else {
        		admin.sendPrivateText("nie powiodło się usuwanie zmiany.");
        	}
        	return;
        	
        } else if(command.equals("alter")) {
        	if(arguments.size()<3) {
        		usage(admin);
        		return;
        	}
        	// first delete transition, then add
        	final String label=arguments.get(0);
        	// TODO: split triggers
        	final String trigger=arguments.get(1);
        	final String text=arguments.get(2);
        	if(label.equals("")) {
        		admin.sendPrivateText("nazwa nie powinna być pusta dla "+command);
        		return;
        	}
        	npc.del(label);
        	npc.add(ConversationStates.ANY, trigger, null, ConversationStates.ANY, text, null, label);
        	return;
        	
        } else if(command.equals("list")) {
        	// no arguments here
        	StringBuilder sb = new StringBuilder();
        	List<Transition> tr = npc.getTransitions();
        	for(Transition t: tr) {
        		sb.append("Zmiany: ("+t.toString()+")\n");
        	}
    		admin.sendPrivateText(sb.toString());
    		return;
    		
        } else {
        	admin.sendPrivateText("nieznane polecenie ("+command+")");
        	return;
        }
	}
}
