package com.github.tgstation.fastdmm.editing;

import com.github.tgstation.fastdmm.FastDMM;
import com.github.tgstation.fastdmm.dmmmap.Location;
import com.github.tgstation.fastdmm.dmmmap.TileInstance;
import com.github.tgstation.fastdmm.objtree.ModifiedType;
import com.github.tgstation.fastdmm.objtree.ObjInstance;

public class EditVarsListener extends SimpleContextMenuListener {
	
	public EditVarsListener(FastDMM editor, Location mapLocation, ObjInstance instance) {
		super(editor, mapLocation, instance);
	}

	@Override
	public void doAction() {
		TileInstance ti;
		synchronized(editor) {
			String key = editor.dmm.map.get(location);
			if(key == null)
				return;
			ti = editor.dmm.instances.get(key);
			if(ti == null)
				return;
		}
		ModifiedType mt = ModifiedType.deriveFrom(oInstance);
		if(!mt.viewVariables(editor).doReturnTrue)
			return;

		synchronized(editor) {
			if(editor.modifiedTypes.containsKey(mt.toString())) {
				mt = editor.modifiedTypes.get(mt.toString());
			} else {
				editor.modifiedTypes.put(mt.toString(), mt);
				if(mt.parent != null) {
					mt.parent.addInstance(mt);
				}
			}
		}
		String newKey = ti.replaceObject(oInstance, mt.vars.size() != 0 ? mt : mt.parent);
		
		editor.dmm.putMap(location, newKey);
	}
}
