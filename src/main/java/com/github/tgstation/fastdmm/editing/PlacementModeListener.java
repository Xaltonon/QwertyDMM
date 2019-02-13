package com.github.tgstation.fastdmm.editing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.github.tgstation.fastdmm.FastDMM;
import com.github.tgstation.fastdmm.editing.placement.PlacementMode;
import com.github.tgstation.fastdmm.editing.placement.SelectPlacementMode;

public class PlacementModeListener implements ActionListener {
	private FastDMM editor;
	private PlacementMode mode;
	
	public PlacementModeListener(FastDMM editor, PlacementMode mode) {
		this.editor = editor;
		this.mode = mode;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		synchronized(editor) {
			if(editor.placementMode != null)
				editor.placementMode.flush(editor);
			editor.placementMode = mode;
			editor.modeLabelString = mode.getLabelText();
			editor.modeLabel.setText(editor.modeLabelString);
			if (mode instanceof SelectPlacementMode) {
				editor.selMode = true;
			} else {
				editor.selMode = false;
			}
		}
	}
}
