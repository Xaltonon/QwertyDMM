package com.github.tgstation.fastdmm.editing.placement;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import com.github.tgstation.fastdmm.FastDMM;
import com.github.tgstation.fastdmm.dmirender.IconState;
import com.github.tgstation.fastdmm.dmirender.RenderInstance;
import com.github.tgstation.fastdmm.dmmmap.Location;
import com.github.tgstation.fastdmm.dmmmap.TileInstance;
import com.github.tgstation.fastdmm.editing.FloatingSelection;
import com.github.tgstation.fastdmm.editing.ui.ModifiedTypeTableModel;
import com.github.tgstation.fastdmm.objtree.ModifiedType;
import com.github.tgstation.fastdmm.objtree.ObjInstance;

public class SelectPlacementMode implements PlacementMode {
    public FastDMM editor;
    
    
    public Set<Location> selection = new HashSet<Location>();
    FloatingSelection floatSelect;
    @Override
    public PlacementHandler getPlacementHandler(FastDMM editor, ObjInstance instance, Location initialLocation) {

    	
        this.editor = editor;
        
        if(selection.contains(initialLocation) && !editor.isAltPressed && !editor.isCtrlPressed && !editor.isShiftPressed) {
        	floatSelect = new FloatingSelection(editor.dmm, selection, editor);
			synchronized(editor) {
				HashMap<Location, String[]> changes = new HashMap<Location, String[]>();
				for(Location l : selection) {
					String key = editor.dmm.map.get(l);
					if(key == null)
						continue;
					TileInstance ti = editor.dmm.instances.get(key);
					if(ti == null)
						continue;
				
					String newKey = ti.deleteAllInFilter(editor);
					
					String[] keys = {key, newKey};
					changes.put(l, keys);
					
					editor.dmm.putMap(l, newKey);
				}
				editor.addToUndoStack(editor.dmm.popDiffs());
			}
			clearSelection();
        }
        
        if(floatSelect != null) {
        	if(floatSelect.inSelection(initialLocation)) {
        		return new MoveFloatingSelectionPlacementHandler();
        	} else {
        		floatSelect.anchor(editor.dmm);
        		floatSelect = null;
        		return null;
        	}
        }
        
        
        return new SelectPlacementHandler();
    }
    
	public String getLabelText() {
		return "Select Mode ";
	}

    @Override
    public int visualize(Set<RenderInstance> rendInstanceSet, int currCreationIndex) {
        if(editor == null)
            return currCreationIndex;
        for(Location l : selection) {
            int dirs = 0;
            for(int i = 0; i < 4; i++) {
                int cdir = IconState.indexToDirArray[i];
                Location l2 = l.getStep(cdir);
                if(!selection.contains(l2))
                    dirs |= cdir;
            }
            if(dirs != 0) {
                RenderInstance ri = new RenderInstance(currCreationIndex++);
                ri.plane = 101;
                ri.x = l.x;
                ri.y = l.y;
                ri.substate = editor.interface_dmi.getIconState("" + dirs).getSubstate(2);
                ri.color = new Color(255,255,255);

                rendInstanceSet.add(ri);
            }
        }
        setCount();
        if(floatSelect != null) {
        	currCreationIndex = floatSelect.visualize(rendInstanceSet, currCreationIndex, editor);
        }
        return currCreationIndex;
    }

    public void setCount() {
        if(selection.size() > 1) {
            editor.modeLabelString = selection.size() + " tiles selected. ";
        } else if(selection.size() == 1) {
            editor.modeLabelString = selection.size() + " tile selected. ";
        } else if(selection.size() == 0) {
            editor.modeLabelString = "No tiles selected. ";
        }
        editor.modeLabel.setText(editor.modeLabelString);
    }

    public void clearSelection() {
        selection.clear();
    }

	@Override
	public void addToTileMenu(FastDMM editor, Location mapLocation, TileInstance instance, JPopupMenu menu) {
		this.editor = editor;
		if(selection.contains(mapLocation)) {
			JMenuItem item = new JMenuItem("Delete Selection");
			item.addActionListener(new SelectPlacementMode.SelectListener(editor, this, true, false));
			menu.add(item);
			item = new JMenuItem("Fill Selection");
			item.addActionListener(new SelectPlacementMode.FillListener(editor, this));
			menu.add(item);
			item = new JMenuItem("Copy Selection");
			item.addActionListener(new SelectPlacementMode.SelectListener(editor, this, false, true));
			menu.add(item);
			item = new JMenuItem("Cut Selection");
			item.addActionListener(new SelectPlacementMode.SelectListener(editor, this, true, true));
			menu.add(item);
			item = new JMenuItem("Edit Vars");
			item.addActionListener(new SelectPlacementMode.EditVarsInSelectionListener(editor, this));
			menu.add(item);	
		}
		if(floatSelect != null && mapLocation.x >= floatSelect.x && mapLocation.x >= floatSelect.x && 
			mapLocation.x < (floatSelect.x+floatSelect.width) && mapLocation.y < (floatSelect.y+floatSelect.height)) {
			
			JMenuItem item = new JMenuItem("Delete Selection");
			item.addActionListener(new SelectPlacementMode.FloatingSelectionListener(editor, this, true, false));
			menu.add(item);
			item = new JMenuItem("Fill Selection");
			item.addActionListener(new SelectPlacementMode.FillListener(editor, this));
			menu.add(item);
			item = new JMenuItem("Copy Selection");
			item.addActionListener(new SelectPlacementMode.FloatingSelectionListener(editor, this, false, true));
			menu.add(item);
			item = new JMenuItem("Cut Selection");
			item.addActionListener(new SelectPlacementMode.FloatingSelectionListener(editor, this, true, true));
			menu.add(item);
			item = new JMenuItem("Edit Vars");
			item.addActionListener(new SelectPlacementMode.EditVarsInSelectionListener(editor, this));
			menu.add(item);	
		}
		JMenuItem item = new JMenuItem("Paste");
		item.addActionListener(new SelectPlacementMode.PasteListener(editor, this, mapLocation));
		menu.add(item);
	}
	
	@Override
	public void flush(FastDMM editor) {
		if(floatSelect != null) {
			floatSelect.anchor(editor.dmm);
		}
	}


	public static class SelectListener implements ActionListener {
		FastDMM editor;
		SelectPlacementMode selection;
		boolean doDelete;
		boolean doCopy;
		
		public SelectListener(FastDMM editor, SelectPlacementMode selection, boolean doDelete, boolean doCopy) {
			this.editor = editor;
			this.selection = selection;
			this.doDelete = doDelete;
			this.doCopy = doCopy;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			synchronized(editor) {
				if(editor.dmm == null)
					return;
				if(doCopy) {
					new FloatingSelection(editor.dmm, selection.selection, editor).toClipboard();
				}
				if(doDelete) {
					HashMap<Location, String[]> changes = new HashMap<Location, String[]>();
					for(Location l : selection.selection) {
						String key = editor.dmm.map.get(l);
						if(key == null)
							continue;
						TileInstance ti = editor.dmm.instances.get(key);
						if(ti == null)
							continue;
					
						String newKey = ti.deleteAllInFilter(editor);
						
						String[] keys = {key, newKey};
						changes.put(l, keys);
						
						editor.dmm.putMap(l, newKey);
					}
					editor.addToUndoStack(editor.dmm.popDiffs());
					selection.clearSelection();
				}
			}
		}
	}
	
	public static class FloatingSelectionListener implements ActionListener {
		FastDMM editor;
		SelectPlacementMode selection;
		boolean doDelete;
		boolean doCopy;
		
		public FloatingSelectionListener(FastDMM editor, SelectPlacementMode selection, boolean doDelete, boolean doCopy) {
			this.editor = editor;
			this.selection = selection;
			this.doDelete = doDelete;
			this.doCopy = doCopy;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			synchronized(editor) {
				if(editor.dmm == null)
					return;
				if(doCopy) {
					selection.floatSelect.toClipboard();
				}
				if(doDelete) {
					selection.floatSelect = null;
				}
			}
		}
	}
	
	public static class PasteListener implements ActionListener {
		FastDMM editor;
		SelectPlacementMode selection;
		Location mapLocation;
		
		public PasteListener(FastDMM editor, SelectPlacementMode selection, Location l) {
			this.editor = editor;
			this.selection = selection;
			this.mapLocation = l;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			synchronized(editor) {
				if(editor.dmm == null)
					return;
				FloatingSelection newFloatSel = FloatingSelection.fromClipboard(editor.objTree,editor.dmm);
				if(newFloatSel == null)
					return;
				newFloatSel.x = mapLocation.x-(newFloatSel.width/2);
				newFloatSel.y = mapLocation.y-(newFloatSel.height/2);
				newFloatSel.z = mapLocation.z;
				selection.clearSelection();
				selection.floatSelect = newFloatSel;
			}
		}
	}
	
	public static class FillListener implements ActionListener {
		FastDMM editor;
		SelectPlacementMode selection;
		Location mapLocation;
		ObjInstance oInstance;
		
		public FillListener(FastDMM editor, SelectPlacementMode selection) {
			this.editor = editor;
			this.selection = selection;
			this.oInstance = editor.selectedInstance;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			if (oInstance != null)
				synchronized(editor) {
					HashMap<Location, String[]> changes = new HashMap<Location, String[]>();
					for(Location l : selection.selection) {
						String key = editor.dmm.map.get(l);
						if(key == null)
							continue;
						TileInstance ti = editor.dmm.instances.get(key);
						if(ti == null)
							continue;
					
						String newKey = ti.addObject(oInstance);
						
						String[] keys = {key, newKey};
						changes.put(l, keys);
						
						editor.dmm.putMap(l, newKey);
					}
					editor.addToUndoStack(editor.dmm.popDiffs());
				}
			
		}
	}
	
	
	
	public static class EditVarsInSelectionListener implements ActionListener {
		FastDMM editor;
		SelectPlacementMode selection;
		Location mapLocation;
		ObjInstance oInstance;
		
		public EditVarsInSelectionListener(FastDMM editor, SelectPlacementMode selection) {
			this.editor = editor;
			this.selection = selection;
			this.oInstance = editor.selectedInstance;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			
			if (oInstance != null) {
				
				ModifiedType mt = ModifiedType.deriveFrom(oInstance);
				
				
				ModifiedTypeTableModel model = mt.viewVariables(editor);
			
				if(!model.doReturnTrue)
					return;

				synchronized(editor) {
					
					for(Location l : selection.selection) {
						String key = editor.dmm.map.get(l);
						if(key == null)
							continue;
						TileInstance ti = editor.dmm.instances.get(key);
						if(ti == null)
							continue;
						
						for (ObjInstance o : ti.objs) {
							if (o != null) {
								if (mt.typeString() != null) {
									if (o.istype(mt.typeString())) {
										ModifiedType m = ModifiedType.deriveFrom(o);
										for (String var : model.editedVals.keySet()) {
											m.vars.put(var, model.editedVals.get(var));
											
										}
										String newKey = ti.replaceObject(o, m);
										editor.dmm.putMap(l, newKey);
									}
								}
							}
						}
					}
					
					editor.addToUndoStack(editor.dmm.popDiffs());
				
				}
			}
		}
	}
}
