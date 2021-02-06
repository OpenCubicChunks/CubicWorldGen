package io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.gui;

import net.malisis.core.client.gui.component.container.UIContainer;

public class UIMultilayerTerrainPreview extends UIContainer<UIMultilayerTerrainPreview> {

	private final UITerrainPreview.TerrainPreviewDataAccess dataAccess;

	public UIMultilayerTerrainPreview(CustomCubicGui gui, UITerrainPreview.TerrainPreviewDataAccess dataAccess) {
		super(gui);
		this.dataAccess = dataAccess;
	}

}
