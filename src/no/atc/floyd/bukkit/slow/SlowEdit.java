package no.atc.floyd.bukkit.slow;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
//import org.bukkit.event.Event.Priority;
import org.bukkit.event.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;


//import com.nijikokun.bukkit.Permissions.Permissions;
//import ru.tehkode.permissions.PermissionManager;
// Simply use: if (player.hasPermission("node.subnode"))


/**
 * Editor plugin for Bukkit
 *
 * /edit list
 * /edit size
 * /edit save <filename>
 * /edit load <filename>
 * /edit scale <factor> [<axis>] <edge|center|here> 
 * /edit rotate <degrees> <axis> [<center|here>]
 * /edit merge
 * /edit reset
 * /edit redo [<player>]
 * 
 * OPTIONS:
 * -ignore <blockdef>[,...]
 * -replace <blockdef:blockdef>[,...]
 * 
 * @author FloydATC
*/
public class SlowEdit extends JavaPlugin implements Listener {
	public static final ConcurrentHashMap<String,SlowTask> tasks = new ConcurrentHashMap<String,SlowTask>(); 

    public void onDisable() {
        // TODO: Place any custom disable code here

        // NOTE: All registered events are automatically unregistered when a plugin is disabled
    	tasks.clear();
    }

    public void onEnable() {
        // TODO: Place any custom enable code here including the registration of any events
    	
        // Register our events
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(this, this);
                
    }

    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
    	SlowTask t = myTask(player);
    	if (t.isRunning()) {
    		respond(player, "§7[§6Slow§7]§b "+t.getStatus());
    	}
    }

    @EventHandler
    public boolean onPlayerInteract( PlayerInteractEvent event ) {
        if (event.hasBlock()) {
            Player p = event.getPlayer();
	    	ItemStack holding = p.getItemInHand();
	    	
	    	// Is the player holding a wooden axe?
	    	if (holding.getType() == Material.WOOD_AXE && p.hasPermission("slowedit.copy")) {
	        	SlowTask t = myTask(p);
		    	Location loc = event.getClickedBlock().getLocation();
		    	if (t.isRunning()) {
					respond(p, "§7[§6Slow§7]§c A task is already running, please wait");
		    	} else {
					if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
						t.setFrom(loc);
    					respond(p, "§7[§6Slow§7]§b OK, first block location set");
					}
					if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
						t.setTo(loc);
    					respond(p, "§7[§6Slow§7]§b OK, second block location set");
					}
		    	} 
	    	}
        }
    	return true;
    }    
   

    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args ) {
    	String cmdname = cmd.getName().toLowerCase();
        Player p = null;
        String pname = "(Console)";
        SlowTask t = null;
        if (sender instanceof Player) {
        	p = (Player) sender;
        	pname = p.getName();
        	t = myTask(p);
        }
        
    	if (cmdname.equalsIgnoreCase("edit")) {
    		if (p == null || p.hasPermission("slowedit.copy")) {
    			getLogger().info(pname+": "+Arrays.toString(args));
    			
    			// "edit" or "edit help"
    			if (args.length == 0 || (args.length == 1 && args[0].equalsIgnoreCase("help")) ) {
    				respond(p, "§7[§6Slow§7]§b /edit from|to");
    				respond(p, "§7[§6Slow§7]§b /edit floor|ceiling <altitude>");
    				respond(p, "§7[§6Slow§7]§b /edit expand|contract <distance> <direction> [...]");
    				respond(p, "§7[§6Slow§7]§b /edit move <distance> <direction> [...]");
    				respond(p, "§7[§6Slow§7]§b /edit flip <direction> [<edge|center|here>]");
    				respond(p, "§7[§6Slow§7]§b /edit preview-on|preview-off");
    				respond(p, "§7[§6Slow§7]§b /edit copy|paste");
    				respond(p, "§7[§6Slow§7]§b /edit undo|status|done");
    				return true;
    			}
    			
    			// "edit from" 
    			// Set first point of cuboid to copy from
    			if (args.length == 1 && args[0].equalsIgnoreCase("from") && t != null) {
    				if (t.isRunning()) {
    					respond(p, "§7[§6Slow§7]§c A task is already running, please wait");
    					return true;
    				} else {
    					t.setFrom(p.getLocation());
    					respond(p, "§7[§6Slow§7]§b OK, first ground location set");
    					return true;
    				}
    			}
    			
    			// "edit to" 
    			// Set second point of cuboid to copy from
    			if (args.length == 1 && args[0].equalsIgnoreCase("to") && t != null) {
    				if (t.isRunning()) {
    					respond(p, "§7[§6Slow§7]§c A task is already running, please wait");
    					return true;
    				} else {
    					t.setTo(p.getLocation());
    					respond(p, "§7[§6Slow§7]§b OK, second ground location set");
    					return true;
    				}
    			}
    			
    			// "edit copy" 
    			// Set source world and reference point
    			if (args.length == 1 && args[0].equalsIgnoreCase("copy") && t != null) {
    				if (t.isRunning()) {
    					respond(p, "§7[§6Slow§7]§c A task is already running, please wait");
    					return true;
    				} else {
    					t.setCopy(p.getLocation());
    					respond(p, "§7[§6Slow§7]§b OK, source reference point has been set");
    					return true;
    				}
    			}
    			
    			// "edit paste" 
    			// Set destination world and reference point
    			if (args.length == 1 && args[0].equalsIgnoreCase("paste") && t != null) {
    				if (t.isRunning()) {
    					respond(p, "§7[§6Slow§7]§c A task is already running, please wait");
    					return true;
    				} else {
    					t.setPaste(p.getLocation());
    					respond(p, "§7[§6Slow§7]§b OK, destination reference point has been set");
    					return true;
    				}
    			}
    			
    			// "edit floor"
    			// Override the lowest y coordinate of the selected cuboid
    			if (args.length == 2 && args[0].equalsIgnoreCase("floor") && t != null) {
    				if (t.isRunning()) {
    					respond(p, "§7[§6Slow§7]§c A task is already running, please wait");
    					return true;
    				} else {
    					Integer alt = null;
    					try {
    						alt = Integer.valueOf(args[1]);
    					} catch (Exception e) {
    						respond(p, "§7[§6Slow§7]§c Expected an integer");
    						return false;
    					}
    					t.setFloor(alt);
    					respond(p, "§7[§6Slow§7]§b OK, floor altitude adjusted");
    					return true;
    				}
    			}
    			
    			// "edit ceiling"
    			// Override the highest y coordinate of the selected cuboid
    			if (args.length == 2 && args[0].equalsIgnoreCase("ceiling") && t != null) {
    				if (t.isRunning()) {
    					respond(p, "§7[§6Slow§7]§c A task is already running, please wait");
    					return true;
    				} else {
    					Integer alt = null;
    					try {
    						alt = Integer.valueOf(args[1]);
    					} catch (Exception e) {
    						respond(p, "§7[§6Slow§7]§c Expected an integer");
    						return false;
    					}
    					t.setCeiling(alt);
    					respond(p, "§7[§6Slow§7]§b OK, ceiling altitude adjusted");
    					return true;
    				}
    			}
    			
    			// "edit move"
    			if (args.length >= 1 && args[0].equalsIgnoreCase("move") && t != null) {
    				if (t.isRunning()) {
    					respond(p, "§7[§6Slow§7]§c A task is already running, please wait");
    					return true;
    				} else {
    					Integer move_n = 0; 
    					Integer move_e = 0; 
    					Integer move_u = 0; 
    					// Get pairs of arguments; distance and direction
    					for (Integer i = 1; i < args.length; i += 2) {
    						Integer amount = 0;
    						try {
    							amount = Integer.parseInt(args[i]);
    						} catch (Exception e) {
    	    					respond(p, "§7[§6Slow§7]§c Expected integer, got '"+args[i]+"'");
    	    					return true;
    						}
    						if (args.length < i+2) {
    	    					respond(p, "§7[§6Slow§7]§c Please specify one or more pair of <distance> and <direction>");
    	    					return true;
    						}
    						if (args[i+1].startsWith("n")) {
    							move_n += amount;
    							continue;
    						}
    						if (args[i+1].startsWith("s")) {
    							move_n -= amount;
    							continue;
    						}
    						if (args[i+1].startsWith("e")) {
    							move_e += amount;
    							continue;
    						}
    						if (args[i+1].startsWith("w")) {
    							move_e -= amount;
    							continue;
    						}
    						if (args[i+1].startsWith("u")) {
    							move_u += amount;
    							continue;
    						}
    						if (args[i+1].startsWith("d")) {
    							move_u -= amount;
    							continue;
    						}
	    					respond(p, "§7[§6Slow§7]§c Invalid direction '"+args[i+1]+"'");
    						return true;
    					}
    					// Parameters parsed ok, apply the movements
    					respond(p, "§7[§6Slow§7]§b OK, will move as specified");
    					if (move_n != 0 ) { 
    						t.moveNorth(move_n); 
    					} 
    					if (move_e != 0) { 
    						t.moveEast(move_e); 
    					} 
    					if (move_u != 0) { 
    						t.moveUp(move_u);
    					} 
    					return true;
    				}
    			}
    			
    			// "edit expand"
    			if (args.length >= 1 && args[0].equalsIgnoreCase("expand") && t != null) {
    				if (t.isRunning()) {
    					respond(p, "§7[§6Slow§7]§c A task is already running, please wait");
    					return true;
    				} else {
    					Integer expand_n = 0; 
    					Integer expand_s = 0; 
    					Integer expand_e = 0; 
    					Integer expand_w = 0; 
    					Integer expand_u = 0; 
    					Integer expand_d = 0; 
    					// Get pairs of arguments; distance and direction
    					for (Integer i = 1; i < args.length; i += 2) {
    						Integer amount = 0;
    						try {
    							amount = Integer.parseInt(args[i]);
    						} catch (Exception e) {
    	    					respond(p, "§7[§6Slow§7]§c Expected integer, got '"+args[i]+"'");
    	    					return true;
    						}
    						if (args.length < i+2) {
    	    					respond(p, "§7[§6Slow§7]§c Please specify one or more pair of <distance> and <direction>");
    	    					return true;
    						}
    						if (args[i+1].startsWith("n")) {
    							expand_n += amount;
    							continue;
    						}
    						if (args[i+1].startsWith("s")) {
    							expand_s += amount;
    							continue;
    						}
    						if (args[i+1].startsWith("e")) {
    							expand_e += amount;
    							continue;
    						}
    						if (args[i+1].startsWith("w")) {
    							expand_w += amount;
    							continue;
    						}
    						if (args[i+1].startsWith("u")) {
    							expand_u += amount;
    							continue;
    						}
    						if (args[i+1].startsWith("d")) {
    							expand_d += amount;
    							continue;
    						}
	    					respond(p, "§7[§6Slow§7]§c Invalid direction '"+args[i+1]+"'");
    						return true;
    					}
    					// Parameters parsed ok, apply the movements
    					respond(p, "§7[§6Slow§7]§b OK, selection area expanded");
    					if (expand_n != 0 ) { 
    						t.expandNorth(expand_n); 
    					} 
    					if (expand_s != 0 ) { 
    						t.expandSouth(expand_s); 
    					} 
    					if (expand_e != 0 ) { 
    						t.expandEast(expand_e); 
    					} 
    					if (expand_w != 0 ) { 
    						t.expandWest(expand_w); 
    					} 
    					if (expand_u != 0 ) { 
    						t.expandUp(expand_u); 
    					} 
    					if (expand_d != 0 ) { 
    						t.expandDown(expand_d); 
    					} 
    					return true;
    				}
    			}
    			
    			// "edit contract"
    			if (args.length >= 1 && args[0].equalsIgnoreCase("contract") && t != null) {
    				if (t.isRunning()) {
    					respond(p, "§7[§6Slow§7]§c A task is already running, please wait");
    					return true;
    				} else {
    					Integer contract_n = 0; 
    					Integer contract_s = 0; 
    					Integer contract_e = 0; 
    					Integer contract_w = 0; 
    					Integer contract_u = 0; 
    					Integer contract_d = 0; 
    					// Get pairs of arguments; distance and direction
    					for (Integer i = 1; i < args.length; i += 2) {
    						Integer amount = 0;
    						try {
    							amount = Integer.parseInt(args[i]);
    						} catch (Exception e) {
    	    					respond(p, "§7[§6Slow§7]§c Expected integer, got '"+args[i]+"'");
    	    					return true;
    						}
    						if (args.length < i+2) {
    	    					respond(p, "§7[§6Slow§7]§c Please specify one or more pair of <distance> and <direction>");
    	    					return true;
    						}
    						if (args[i+1].startsWith("n")) {
    							contract_n += amount;
    							continue;
    						}
    						if (args[i+1].startsWith("s")) {
    							contract_s += amount;
    							continue;
    						}
    						if (args[i+1].startsWith("e")) {
    							contract_e += amount;
    							continue;
    						}
    						if (args[i+1].startsWith("w")) {
    							contract_w += amount;
    							continue;
    						}
    						if (args[i+1].startsWith("u")) {
    							contract_u += amount;
    							continue;
    						}
    						if (args[i+1].startsWith("d")) {
    							contract_d += amount;
    							continue;
    						}
	    					respond(p, "§7[§6Slow§7]§c Invalid direction '"+args[i+1]+"'");
    						return true;
    					}
    					// Parameters parsed ok, apply the movements
    					respond(p, "§7[§6Slow§7]§b OK, selection area contracted");
    					if (contract_n != 0 ) { 
    						t.contractNorth(contract_n); 
    					} 
    					if (contract_s != 0 ) { 
    						t.contractSouth(contract_s); 
    					} 
    					if (contract_e != 0 ) { 
    						t.contractEast(contract_e); 
    					} 
    					if (contract_w != 0 ) { 
    						t.contractWest(contract_w); 
    					} 
    					if (contract_u != 0 ) { 
    						t.contractUp(contract_u); 
    					} 
    					if (contract_d != 0 ) { 
    						t.contractDown(contract_d); 
    					} 
    					return true;
    				}
    			}
    			
    			// "edit flip"
    			if (args.length >= 1 && args[0].equalsIgnoreCase("flip") && t != null) {
    				if (t.isRunning()) {
    					respond(p, "§7[§6Slow§7]§c A task is already running, please wait");
    					return true;
    				} else {
    					if (args.length == 1 || args.length > 3) {
    						respond(p, "§7[§6Slow§7]§b Syntax:");
    						respond(p, "§7[§6Slow§7]§b /edit flip up|down|north|south|east|west [edge|center|here]");
    						return true;
    					}
    					if (args.length == 3) {
    						// TODO: Get methods "center", "edge" or "here"
    						respond(p, "§7[§6Slow§7]§c Alternate flip methods are not yet supported");
    						return true;
    					}
    					
    					// Flip in the specified direction
    					if (args[1].startsWith("n")) {
    						t.flipNorthSouth();
        					respond(p, "§7[§6Slow§7]§b OK, will flip as specified");
    						return true;
    					}
    					if (args[1].startsWith("s")) {
    						t.flipNorthSouth();
        					respond(p, "§7[§6Slow§7]§b OK, will flip as specified");
    						return true;
    					}
    					if (args[1].startsWith("e")) {
    						t.flipEastWest();
        					respond(p, "§7[§6Slow§7]§b OK, will flip as specified");
    						return true;
    					}
    					if (args[1].startsWith("w")) {
    						t.flipEastWest();
        					respond(p, "§7[§6Slow§7]§b OK, will flip as specified");
    						return true;
    					}
    					if (args[1].startsWith("u")) {
    						t.flipUpDown();
        					respond(p, "§7[§6Slow§7]§b OK, will flip as specified");
    						return true;
    					}
    					if (args[1].startsWith("d")) {
    						t.flipUpDown();
        					respond(p, "§7[§6Slow§7]§b OK, will flip as specified");
    						return true;
    					}
						respond(p, "§7[§6Slow§7]§c Expected a flip direction, got '"+args[0]+"'");
    					return true;
    				}
    			}
    			
    			// "edit rotate"
    			if (args.length >= 1 && args[0].equalsIgnoreCase("rotate") && t != null) {
    				if (t.isRunning()) {
    					respond(p, "§7[§6Slow§7]§c A task is already running, please wait");
    					return true;
    				} else {
    					if (args.length == 1 || args.length > 4) {
    						respond(p, "§7[§6Slow§7]§b Syntax:");
    						respond(p, "§7[§6Slow§7]§b /edit rotate <degrees> x|y|z [edge|center|here]");
    						return true;
    					}

    					// Get the cartesian rotation angle
    					Integer degrees = 0;
    					try {
    						degrees = Integer.parseInt(args[1]);
    					} catch (Exception e) {
    						respond(p, "§7[§6Slow§7]§c Expected a rotation angle, got '"+args[1]+"'");
    						return true;
    					}
    					
    					// Get rotation axis (y is default)
    					String axis = "y";
    					if (args.length >= 3) {
    						axis = args[2].toLowerCase();
    					}

    					// Get reference point, if any
    					if (args.length == 4) {
    						// TODO: Get methods "center", "edge" or "here"
    						respond(p, "§7[§6Slow§7]§c Alternate flip methods are not yet supported");
    						return true;
    					}
    					
    					// Rotate around the specified axis
    					if (axis.equals("x")) {
    						t.rotateX(degrees);
        					respond(p, "§7[§6Slow§7]§b OK, rotate as specified");
    						return true;
    					}
    					if (axis.equals("y")) {
    						t.rotateY(degrees);
        					respond(p, "§7[§6Slow§7]§b OK, rotate as specified");
    						return true;
    					}
    					if (axis.equals("z")) {
    						t.rotateZ(degrees);
        					respond(p, "§7[§6Slow§7]§b OK, rotate as specified");
    						return true;
    					}
						respond(p, "§7[§6Slow§7]§c Expected a rotation axis (x, y or z), got '"+args[2]+"'");
    					return true;
    				}
    			}
    			
    			// "edit execute" 
    			// Start copy job as a background thread
    			if (args.length == 1 && args[0].equalsIgnoreCase("execute") && t != null) {
    				if (!t.isValid()) {
    					respond(p, "§7[§6Slow§7]§c One or more parameters missing");
    					return true;
    				}
    				if (t.setTask("copy")) {
       					respond(p, "§7[§6Slow§7]§b Background copy/paste started");
   					} else {
    					respond(p, "§7[§6Slow§7]§c A task is already running, please wait");
   					}
					return true;
    			}
    			
    			// "edit undo" 
    			// Start undo job as a background thread
    			if (args.length == 1 && args[0].equalsIgnoreCase("undo") && t != null) {
    				if (!t.isUndoable()) {
    					respond(p, "§7[§6Slow§7]§b Nothing to undo yet");
    					return true;
    				}
    				if (t.setTask("undo")) {
       					respond(p, "§7[§6Slow§7]§b Background undo started");
       					return true;
   					} else {
    					respond(p, "§7[§6Slow§7]§c A task is already running, please wait");
    					return true;
   					}
    			}
    			
    			// "edit preview-on" 
    			// Start copy job as a background thread
    			if (args.length == 1 && args[0].equalsIgnoreCase("preview-on") && t != null) {
    				if (!t.isValid()) {
    					respond(p, "§7[§6Slow§7]§c One or more parameters missing");
    					return true;
    				}
    				if (t.setTask("preview-on")) {
       					respond(p, "§7[§6Slow§7]§b Background preview-on started");
   					} else {
    					respond(p, "§7[§6Slow§7]§c A task is already running, please wait");
   					}
   					return true;
    			}
    			
    			// "edit preview-off" 
    			// Start copy job as a background thread
    			if (args.length == 1 && args[0].equalsIgnoreCase("preview-off") && t != null) {
    				if (!t.isValid()) {
    					respond(p, "§7[§6Slow§7]§c One or more parameters missing");
    					return true;
    				}
    				if (t.setTask("preview-off")) {
       					respond(p, "§7[§6Slow§7]§b Background preview-off started");
   					} else {
    					respond(p, "§7[§6Slow§7]§c A task is already running, please wait");
   					}
					return true;
    			}
    			
    			
    			// "edit done" or "edit new" 
    			// Allow the background process to terminate
    			if (args.length == 1 && (args[0].equalsIgnoreCase("done") || args[0].equalsIgnoreCase("new")) && t != null) {
    				if (t.finish()) {
       					respond(p, "§7[§6Slow§7]§b OK, released undo buffer and reset parameters");
   					} else {
    					respond(p, "§7[§6Slow§7]§c Please wait, "+t.getStatus());
   					}
					return true;
    			}
    			

    			// "edit status" 
    			// Show current status of copy job
    			if (args.length == 1 && args[0].equalsIgnoreCase("status") && t != null) {
   					respond(p, "§7[§6Slow§7]§b "+t.getStatus());
   					return true;
    			}

    			getLogger().info(pname+": Unknown subcommand");
    		} else {
    			getLogger().info(pname+": Access denied");
    		}
    	}
    	return false;
    }

    private void respond(Player player, String message) {
    	if (player == null) {
        	Server server = getServer();
        	ConsoleCommandSender console = server.getConsoleSender();
        	console.sendMessage(message);
    	} else {
    		player.sendMessage(message);
    	}
    }

    private SlowTask myTask(Player p) {
    	SlowTask t = tasks.get(p.getName());
    	if (t == null) {
    		t = new SlowTask(this, p.getName());
    		tasks.put(p.getName(), t);
    	}
    	return t; 
    }
    
    
}
