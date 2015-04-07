package no.atc.floyd.bukkit.slow;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

public class SlowTask implements Runnable {

	
	private String task = "idle";
	private Integer batch_size = 1024; // Blocks to process per call to run()
	
	private Integer undo = 0;

	// Transformation matrix
	private Matrix4x4 m = new Matrix4x4(1,0,0,0, 0,1,0,0, 0,0,1,0, 0,0,0,0); // Identity
	
	// Define source area to copy
	private Location src_p1 = new Location(null, 0, 0, 0);
	private Location src_p2 = new Location(null, 0, 0, 0);
	
	// Reference points for copy/paste operation
	private Location copy = new Location(null, 0, 0, 0);
	private Location paste = new Location(null, 0, 0, 0);
	
	// Progress stats
	private Integer ops_total = 0;
	private Integer ops_completed = 0;

	// Run state variables
	private Integer x1 = 0;
	private Integer x2 = 0;
	private Integer y1 = 0;
	private Integer y2 = 0;
	private Integer z1 = 0;
	private Integer z2 = 0;
	private Integer delta_x = paste.getBlockX() - copy.getBlockX();
	private Integer delta_y = paste.getBlockY() - copy.getBlockY();
	private Integer delta_z = paste.getBlockZ() - copy.getBlockZ();
	private Integer x = 0;
	private Integer y = 0;
	private Integer z = 0;
	private Integer to_x = 0;
	private Integer to_y = 0;
	private Integer to_z = 0;
	private Integer t = 0;
	private Vector v = new Vector();
	private Block src = null;
	private Block dst = null;
	private World src_world = copy.getWorld();
	private World dst_world = paste.getWorld();
	private int[][][] src_bid = null;
	private byte[][][] src_bdata = null;
	private int[][][] dst_bid = null;
	private byte[][][] dst_bdata = null;
	private Boolean initialized = false; 

	// Identity
	private Plugin plugin = null;
	private Server server = null;
	private Integer id = -1;
	private String owner = "(Unknown)";
	
	public SlowTask(Plugin plugin, String owner) {
		this.plugin = plugin;
		this.server = plugin.getServer();
		this.owner = owner;
	}
	
	public boolean isRunning() {
		return (id != -1);
	}

	public boolean isIdle() {
		return task.equalsIgnoreCase("idle");
	}

	public boolean isValid() {
		if (copy.getWorld() == null) { return false; }
		if (paste.getWorld() == null) { return false; }
		if (src_p1.getWorld() == null) { return false; }
		if (src_p2.getWorld() == null) { return false; }
		return true;
	}
	
	public boolean isUndoable() {
		return (undo > 0);
	}

	private void initialize() {
		try {
			x1 = lo(src_p1.getBlockX(), src_p2.getBlockX());
			x2 = hi(src_p1.getBlockX(), src_p2.getBlockX());
			y1 = lo(src_p1.getBlockY(), src_p2.getBlockY());
			if (y1 < 0) { y1 = 0; }
			if (y1 > copy.getWorld().getMaxHeight()) { y1 = copy.getWorld().getMaxHeight(); }
			y2 = hi(src_p1.getBlockY(), src_p2.getBlockY());
			if (y2 < 0) { y2 = 0; }
			if (y2 > copy.getWorld().getMaxHeight()) { y2 = copy.getWorld().getMaxHeight(); }
			z1 = lo(src_p1.getBlockZ(), src_p2.getBlockZ());
			z2 = hi(src_p1.getBlockZ(), src_p2.getBlockZ());
			delta_x = paste.getBlockX() - copy.getBlockX();
			delta_y = paste.getBlockY() - copy.getBlockY();
			delta_z = paste.getBlockZ() - copy.getBlockZ();
			ops_total = (1+x2-x1) * (1+y2-y1) * (1+z2-z1);
			ops_completed = 0;
			x = 0;
			y = 0;
			z = 0;
			to_x = 0;
			to_y = 0;
			to_z = 0;
			t = 0;
			v = new Vector();
			src = null;
			dst = null;
			src_world = copy.getWorld();
			dst_world = paste.getWorld();
			src_bid = new int[x2-x1+1][y2-y1+1][z2-z1+1];
			src_bdata = new byte[x2-x1+1][y2-y1+1][z2-z1+1];
			dst_bid = new int[x2-x1+1][y2-y1+1][z2-z1+1];
			dst_bdata = new byte[x2-x1+1][y2-y1+1][z2-z1+1];
			initialized = true;
			plugin.getLogger().info(owner+": task state initialized, total ops="+ops_total);
		}
		catch (Exception e) {
			task = "idle";
			initialized = false;
			plugin.getLogger().warning(owner+": task initialization failed: "+e.getLocalizedMessage());
		}
	}
	
	@Override
	public void run() {
		
		//if (isIdle()) {
		//	return;
		//}
		
		if (initialized == false) {
			this.initialize();
			return;
		}
		Integer fuse = batch_size;
		while (true) {
			fuse--;
			if (fuse == 0) { return; }
			
			if (task.equalsIgnoreCase("preview-on")) {
				// place DIRT at sky level to indicate the paste area
				y = src_world.getMaxHeight() - 1;

				v.setX(x);
				v.setY(y1);
				v.setZ(z);
				v = m.transform(v);	// Rotate, move, scale etc.
				to_x = v.getBlockX() + x1 + delta_x;
				to_y = y;
				to_z = v.getBlockZ() + z1 + delta_z;
				
				dst = dst_world.getBlockAt(to_x, to_y, to_z);
				dst.setTypeId(1);
				//System.out.println(to_x+","+to_y+","+to_z+" = 1");

			    ops_completed++;
			    if (ops_completed % batch_size == 0) {
			    	return;
			    }
			    
			    // Step coordinates X and Z
			    x++;
			    if (x > x2-x1) {
			    	x = 0;
			    	z++;
			    }
			    if (z > z2-z1) {
			    	plugin.getLogger().info(owner+": slow preview-on finished, wrote "+ops_completed+" blocks");
			    	task = "idle";
			    	return;
			    }
			}

			
			if (task.equalsIgnoreCase("preview-off")) {
				// place AIR at sky level to "un-indicate" the paste area
				y = src_world.getMaxHeight() - 1;

				v.setX(x);
				v.setY(y1);
				v.setZ(z);
				v = m.transform(v);	// Rotate, move, scale etc.
				to_x = v.getBlockX() + x1 + delta_x;
				to_y = y;
				to_z = v.getBlockZ() + z1 + delta_z;
				
				dst = dst_world.getBlockAt(to_x, to_y, to_z);
				dst.setTypeId(0);

			    ops_completed++;
			    if (ops_completed % batch_size == 0) {
			    	return;
			    }

			    // Step coordinates X and Z
			    x++;
			    if (x > x2-x1) {
			    	x = 0;
			    	z++;
			    }
			    if (z > z2-z1) {
			    	plugin.getLogger().info(owner+": slow preview-off finished, wrote "+ops_completed+" blocks");
			    	task = "idle";
			    	return;
			    }
			}

			
			if (task.equalsIgnoreCase("copy")) {
				// Phase 1: copy src and dst to memory (dst is for undo)
				//System.out.println("R"+x+","+y+","+z+" ");
				// Get source block
				//System.out.println("world: "+w1.getName());
				//FIXME: need chunk in memory
			    t = src_world.getBlockTypeIdAt(x1+x, y1+y, z1+z);
			    Chunk c = src_world.getChunkAt(x1+x, z1+z);
			    if (c.isLoaded() == false ) { c.load(); }
				src_bid[x][y][z] = t;
			    if (hasData(t)) {
					src = src_world.getBlockAt(x1+x, y1+y, z1+z);
					src_bid[x][y][z] = src.getTypeId();
					src_bdata[x][y][z] = src.getData();
			    } else {
			    	src_bdata[x][y][z] = 0;
			    }

				v.setX(x);
				v.setY(y);
				v.setZ(z);
				v = m.transform(v);	// Rotate, move, scale etc.
				//System.out.println("B"+v.getBlockX()+","+v.getBlockY()+","+v.getBlockZ()+" ");
				to_x = v.getBlockX() + x1 + delta_x;
				to_y = v.getBlockY() + y1 + delta_y;
				to_z = v.getBlockZ() + z1 + delta_z;

				if (to_y >= 0 && to_y <= dst_world.getMaxHeight()) {
					//FIXME: need chunk in memory
				    Chunk c2 = src_world.getChunkAt(to_x, to_z);
				    if (c2.isLoaded() == false ) { c2.load(); }
				    t = dst_world.getBlockTypeIdAt(to_x, to_y, to_z);
					dst_bid[x][y][z] = t;
				    if (hasData(t)) {
						dst = dst_world.getBlockAt(to_x, to_y, to_z);
						dst_bdata[x][y][z] = dst.getData();
				    } else {
				    	dst_bdata[x][y][z] = 0;
				    }
				}

			    ops_completed++;
			    if (ops_completed % batch_size == 0) {
			    	return;
			    }

			    // Step coordinates X, Z and Y
			    y++;
			    if (y > y2-y1) {
			    	y = 0;
			    	x++;
			    }
			    if (x > x2-x1) {
			    	x = 0;
			    	z++;
			    }
			    if (z > z2-z1) {
			    	plugin.getLogger().info(owner+": slow copy finished, read "+ops_completed+" blocks");
					task = "paste";
					x = 0;
					y = 0;
					z = 0;
					ops_completed = 0;
			    	return;
			    }
			}
				
			if (task.equalsIgnoreCase("paste")) {
				// Phase 2: write memory copy of src to dst
				//System.out.println("W"+x+","+y+","+z+" ");
				v.setX(x);
				v.setY(y);
				v.setZ(z);
				v = m.transform(v);	// Rotate, move, scale etc.
				to_x = v.getBlockX() + x1 + delta_x;
				to_y = v.getBlockY() + y1 + delta_y;
				to_z = v.getBlockZ() + z1 + delta_z;
				
				if (to_y >= 0 && to_y <= dst_world.getMaxHeight()) {
					//FIXME: need chunk in memory
				    Chunk c = src_world.getChunkAt(to_x, to_z);
				    if (c.isLoaded() == false ) { c.load(); }
					dst = dst_world.getBlockAt(to_x, to_y, to_z);
				    dst.setTypeIdAndData(src_bid[x][y][z], src_bdata[x][y][z], false);
				}
			    
			    ops_completed++;
			    undo = ops_completed;
			    if (ops_completed % batch_size == 0) {
			    	return;
			    }

			    // Step coordinates X, Z and Y
			    y++;
			    if (y > y2-y1) {
			    	y = 0;
			    	x++;
			    }
			    if (x > x2-x1) {
			    	x = 0;
			    	z++;
			    }
			    if (z > z2-z1) {
			    	plugin.getLogger().info(owner+": slow paste finished, wrote "+ops_completed+" blocks");
					task = "idle";
			    	return;
			    }
			}
			
			if (task.equalsIgnoreCase("undo")) {
				// Phase 1: write memory copy of dst to dst
							
				if (undo > 0) {
					v.setX(x);
					v.setY(y);
					v.setZ(z);
					v = m.transform(v);	// Rotate, move, scale etc.
					to_x = v.getBlockX() + x1 + delta_x;
					to_y = v.getBlockY() + y1 + delta_y;
					to_z = v.getBlockZ() + z1 + delta_z;
					
					if (to_y >= 0 && to_y <= dst_world.getMaxHeight()) {
						//FIXME: need chunk in memory
					    Chunk c = src_world.getChunkAt(to_x, to_z);
					    if (c.isLoaded() == false ) { c.load(); }
						dst = dst_world.getBlockAt(to_x, to_y, to_z);
						dst.setTypeIdAndData(dst_bid[x][y][z], dst_bdata[x][y][z], false);
					}
					
				    ops_completed++;
				    undo--;
				    if (ops_completed % batch_size == 0) {
				    	return;
				    }

				    // Step coordinates X, Z and Y
				    y++;
				    if (y > y2-y1) {
				    	y = 0;
				    	x++;
				    }
				    if (x > x2-x1) {
				    	x = 0;
				    	z++;
				    }
				    if (z > z2-z1) {
				    	plugin.getLogger().info(owner+": slow undo finished, wrote "+ops_completed+" blocks");
						task = "idle";
				    	return;
				    }
				} else {
			    	plugin.getLogger().info(owner+": slow undo finished, wrote "+ops_completed+" blocks");
					task = "idle";
			    	return;
				}
			}

		}
	}

	public String getStatus() {
		Integer percent = 0;
		if (ops_total > 0) {
			percent = ops_completed * 100 / ops_total;	
		}
		if (!isValid()) { return "Waiting for parameters (from/to/copy/paste)"; }
		if (!isRunning()) { return "Not started yet"; }
		if (isIdle()) {
			return "Finished"; 
		} else {
			return "Executing '"+task+"', "+percent+"% complete"; 
		}
	}
	
	public boolean setFrom(Location loc) {
		if (this.isRunning()) { return false; }
		src_p1 = loc.clone();
		//System.out.println("p1 = "+loc.getBlockX()+","+loc.getBlockY()+","+loc.getBlockZ());
		if (src_p1.getWorld() != src_p2.getWorld()) {
			// Setting coordinates in a different world
			//System.out.println("resetting p2");
			src_p2 = src_p1.clone();
		}
		//System.out.println("from: "+src_p1);
		initialized = false;
		return true;
	}
	
	public boolean setTo(Location loc) {
		if (this.isRunning()) { return false; }
		src_p2 = loc.clone();
		//System.out.println("p2 = "+loc.getBlockX()+","+loc.getBlockY()+","+loc.getBlockZ());
		if (src_p2.getWorld() != src_p1.getWorld()) {
			// Setting coordinates in a different world
			//System.out.println("resetting p1");
			src_p1 = src_p2.clone();
		}
		//System.out.println("to: "+src_p2);
		initialized = false;
		return true;
	}

	public boolean setFloor(Integer limit) {
		if (this.isRunning()) { return false; }
		Location lowest = l_down(src_p1, src_p2);
		Location highest = l_up(src_p1, src_p2);
		lowest.setY(limit);
		if (lowest.equals(l_down(src_p1, src_p2)) == false) {
			//System.out.println("selection box was pushed up");
			highest.setY(limit);
		}
		initialized = false;
		return true;
	}

	public boolean setCeiling(Integer limit) {
		if (this.isRunning()) { return false; }
		Location lowest = l_down(src_p1, src_p2);
		Location highest = l_up(src_p1, src_p2);
		highest.setY(limit);
		if (highest.equals(l_up(src_p1, src_p2)) == false) {
			//System.out.println("selection box was pushed down");
			lowest.setY(limit);
		}
		initialized = false;
		return true;
	}

	public boolean setTask(String name) {
		if (this.isRunning() && !this.isIdle()) { return false; }
		if (!this.isValid()) { return false; } 
		task = name;
		x = 0;
		y = 0;
		z = 0;
		ops_completed = 0;
		plugin.getLogger().info(owner+": executing task '"+name+"'");
		if (!this.isRunning()) {
			id = server.getScheduler().scheduleSyncRepeatingTask(plugin, this, 1L, 1L);
			if (id == -1) {
				plugin.getLogger().warning(owner+": scheduleSyncRepeatingTask() failed");
			} else {
				plugin.getLogger().info(owner+": scheduleSyncRepeatingTask() succeeded (id="+id+")");
			}
		}
		return true;
	}


	public boolean finish() {
		if (this.isRunning()) {
			if (!this.isIdle()) { return false; }
			server.getScheduler().cancelTask(id);
			plugin.getLogger().info(owner+": SyncRepeatingTask id="+id+" has been cancelled");
			id = -1;
			initialized = false;
		}
		return true;
	}
	
	public boolean setCopy(Location loc) {
		if (this.isRunning()) { return false; }
		copy = loc.clone();
		if (paste == null) {
			paste = loc.clone();
		}
		if (copy.getWorld() != src_p1.getWorld()) {
			//System.out.println("resetting p1");
			src_p1 = copy.clone();
		}
		if (copy.getWorld() != src_p2.getWorld()) {
			//System.out.println("resetting p2");
			src_p2 = copy.clone();
		}
		//System.out.println("copy: "+copy);
		initialized = false;
		return true;
	}

	public boolean setPaste(Location loc) {
		if (this.isRunning()) { return false; }
		paste = loc.clone();
		if (copy == null) {
			copy = loc.clone();
		}
		//System.out.println("paste: "+paste);
		initialized = false;
		return true;
	}

	public boolean resetTransform(){
		if (this.isRunning()) { return false; }
		m.setIdentity();
		return true;
	}
	
	public boolean expandUp(Integer amount) {
		if (this.isRunning()) { return false; }
		l_up(src_p1, src_p2).add(v_up(amount));
		initialized = false;
		return true;
	}

	public boolean expandDown(Integer amount) {
		if (this.isRunning()) { return false; }
		l_down(src_p1, src_p2).add(v_down(amount));
		initialized = false;
		return true;
	}

	public boolean expandNorth(Integer amount) {
		if (this.isRunning()) { return false; }
		l_north(src_p1, src_p2).add(v_north(amount));
		initialized = false;
		return true;
	}

	public boolean expandSouth(Integer amount) {
		if (this.isRunning()) { return false; }
		l_south(src_p1, src_p2).add(v_south(amount));
		initialized = false;
		return true;
	}

	public boolean expandEast(Integer amount) {
		if (this.isRunning()) { return false; }
		l_east(src_p1, src_p2).add(v_east(amount));
		initialized = false;
		return true;
	}

	public boolean expandWest(Integer amount) {
		if (this.isRunning()) { return false; }
		l_west(src_p1, src_p2).add(v_west(amount));
		initialized = false;
		return true;
	}

	public boolean flipNorthSouth() {
		if (this.isRunning()) { return false; }
		m.multiply(new Matrix4x4(-1,0,0,0, 0,1,0,0, 0,0,1,0, 0,0,0,1));
		return true;
	}

	public boolean flipUpDown() {
		if (this.isRunning()) { return false; }
		m.multiply(new Matrix4x4(1,0,0,0, 0,-1,0,0, 0,0,1,0, 0,0,0,1));
		return true;
	}

	public boolean flipEastWest() {
		if (this.isRunning()) { return false; }
		m.multiply(new Matrix4x4(1,0,0,0, 0,1,0,0, 0,0,-1,0, 0,0,0,1));
		return true;
	}

	public boolean moveUp(Integer amount) {
		if (this.isRunning()) { return false; }
		m.multiply(new Matrix4x4(1,0,0,0, 0,1,0,0, 0,0,1,0, 0,amount,0,1));
		return true;
	}
	
	public boolean moveDown(Integer amount) {
		if (this.isRunning()) { return false; }
		m.multiply(new Matrix4x4(1,0,0,0, 0,1,0,0, 0,0,1,0, 0,-amount,0,1));
		return true;
	}
	
	public boolean moveNorth(Integer amount) {
		if (this.isRunning()) { return false; }
		m.multiply(new Matrix4x4(1,0,0,0, 0,1,0,0, 0,0,1,0, -amount,0,0,1));
		return true;
	}
	
	public boolean moveSouth(Integer amount) {
		if (this.isRunning()) { return false; }
		m.multiply(new Matrix4x4(1,0,0,0, 0,1,0,0, 0,0,1,0, amount,0,0,1));
		return true;
	}
	
	public boolean moveEast(Integer amount) {
		if (this.isRunning()) { return false; }
		m.multiply(new Matrix4x4(1,0,0,0, 0,1,0,0, 0,0,1,0, 0,0,-amount,1));
		return true;
	}
	
	public boolean moveWest(Integer amount) {
		if (this.isRunning()) { return false; }
		m.multiply(new Matrix4x4(1,0,0,0, 0,1,0,0, 0,0,1,0, 0,0,amount,1));
		return true;
	}

	public boolean rotateX(Integer degrees) {
		if (this.isRunning()) { return false; }
		Double r = Math.PI * degrees / 180;
		m.rotateX(r);
		return true;
	}
	
	public boolean rotateY(Integer degrees) {
		if (this.isRunning()) { return false; }
		Double r = Math.PI * degrees / 180;
		m.rotateY(r);
		return true;
	}
	
	public boolean rotateZ(Integer degrees) {
		if (this.isRunning()) { return false; }
		Double r = Math.PI * degrees / 180;
		m.rotateZ(r);
		return true;
	}
	
	public boolean contractUp(Integer amount) {
		if (this.isRunning()) { return false; }
		Location lowest = l_down(src_p1, src_p2);
		Location highest = l_up(src_p1, src_p2);
		lowest.add(v_up(amount));
		if (lowest.equals(l_down(src_p1, src_p2)) == false) {
			highest.setY(lowest.getBlockY());
		}
		initialized = false;
		return true;
	}

	public boolean contractDown(Integer amount) {
		if (this.isRunning()) { return false; }
		Location lowest = l_down(src_p1, src_p2);
		Location highest = l_up(src_p1, src_p2);
		highest.add(v_down(amount));
		if (highest.equals(l_up(src_p1, src_p2)) == false) {
			lowest.setY(highest.getBlockY());
		}
		initialized = false;
		return true;
	}

	public boolean contractNorth(Integer amount) {
		if (this.isRunning()) { return false; }
		Location northern = l_north(src_p1, src_p2);
		Location southern = l_south(src_p1, src_p2);
		southern.add(v_north(amount));
		if (southern.equals(l_south(src_p1, src_p2)) == false) {
			northern.setX(southern.getBlockX());
		}
		initialized = false;
		return true;
	}

	public boolean contractSouth(Integer amount) {
		if (this.isRunning()) { return false; }
		Location northern = l_north(src_p1, src_p2);
		Location southern = l_south(src_p1, src_p2);
		northern.add(v_west(amount));
		if (northern.equals(l_north(src_p1, src_p2)) == false) {
			southern.setX(northern.getBlockX());
		}
		initialized = false;
		return true;
	}

	public boolean contractEast(Integer amount) {
		if (this.isRunning()) { return false; }
		Location eastern = l_east(src_p1, src_p2);
		Location western = l_west(src_p1, src_p2);
		western.add(v_east(amount));
		if (western.equals(l_west(src_p1, src_p2)) == false) {
			eastern.setZ(western.getBlockZ());
		}
		initialized = false;
		return true;
	}

	public boolean contractWest(Integer amount) {
		if (this.isRunning()) { return false; }
		Location eastern = l_east(src_p1, src_p2);
		Location western = l_west(src_p1, src_p2);
		eastern.add(v_west(amount));
		if (eastern.equals(l_east(src_p1, src_p2)) == false) {
			western.setZ(eastern.getBlockZ());
		}
		initialized = false;
		return true;
	}



	private Location l_up(Location l1, Location l2) {
		if (l1.getBlockY() >= l2.getBlockY()) {
			return l1;
		} else {
			return l2;
		}
	}
	
	private Location l_down(Location l1, Location l2) {
		if (l1.getBlockY() <= l2.getBlockY()) {
			return l1;
		} else {
			return l2;
		}
	}
	
	private Location l_south(Location l1, Location l2) {
		if (l1.getBlockX() >= l2.getBlockX()) {
			return l1;
		} else {
			return l2;
		}
	}
	
	private Location l_north(Location l1, Location l2) {
		if (l1.getBlockX() <= l2.getBlockX()) {
			return l1;
		} else {
			return l2;
		}
	}
	
	private Location l_east(Location l1, Location l2) {
		if (l1.getBlockZ() >= l2.getBlockZ()) {
			return l1;
		} else {
			return l2;
		}
	}
	
	private Location l_west(Location l1, Location l2) {
		if (l1.getBlockZ() <= l2.getBlockZ()) {
			return l1;
		} else {
			return l2;
		}
	}
	
	private Vector v_up(Integer distance) {
		Vector v = new Vector(0, distance, 0);
		return v;
	}

	private Vector v_down(Integer distance) {
		Vector v = new Vector(0, -distance, 0);
		return v;
	}

	private Vector v_north(Integer distance) {
		Vector v = new Vector(-distance, 0, 0);
		return v;
	}

	private Vector v_south(Integer distance) {
		Vector v = new Vector(distance, 0, 0);
		return v;
	}

	private Vector v_east(Integer distance) {
		Vector v = new Vector(0, 0, -distance);
		return v;
	}

	private Vector v_west(Integer distance) {
		Vector v = new Vector(0, 0, distance);
		return v;
	}

	private Integer lo(Integer i1, Integer i2) {
		return (i1 <= i2 ? i1 : i2);
	}

	private Integer hi(Integer i1, Integer i2) {
		return (i1 >= i2 ? i1 : i2);
	}

	// Return true for block types that require additional data like color, orientation etc.
	private boolean hasData(Integer id) {
		switch (id) {
			case 0 : return false;
			case 1 : return false;
			case 2 : return false;
			case 3 : return false;
			case 4 : return false;
			case 7 : return false;
			case 12 : return false;
			case 13 : return false;
			case 14 : return false;
			case 15 : return false;
			case 16 : return false;
			case 19 : return false;
			case 20 : return false;
			case 21 : return false;
			case 22 : return false;
			case 30 : return false;
			case 45 : return false;
			case 46 : return false;
			case 47 : return false;
			case 48 : return false;
			case 49 : return false;
			case 51 : return false;
			case 52 : return false;
			case 54 : return false;
			case 56 : return false;
			case 57 : return false;
			case 58 : return false;
			case 73 : return false;
			case 78 : return false;
			case 79 : return false;
			case 80 : return false;
			case 82 : return false;
			case 84 : return false;
			case 85 : return false;
			case 87 : return false;
			case 88 : return false;
			case 89 : return false;
			case 90 : return false;
			case 101 : return false;
			case 102 : return false;
			case 103 : return false;
		}
		return true;
	}
}

