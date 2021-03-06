/*******************************************************************************
 * Copyright (c) OSMCB developers
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package osmcd.gui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;

import osmcd.exceptions.AtlasTestException;
import osmcd.gui.atlastree.JAtlasTree;
import osmcd.program.AtlasThread;
import osmcd.program.interfaces.AtlasInterface;
import osmcd.utilities.I18nUtils;

public class AtlasCreate implements ActionListener
{

	private JAtlasTree jAtlasTree;

	public AtlasCreate(JAtlasTree jAtlasTree) {
		this.jAtlasTree = jAtlasTree;
	}

	public void actionPerformed(ActionEvent event)
	{
		if (!jAtlasTree.testAtlasContentValid())
			return;
		try
		{
			// We have to work on a deep clone otherwise the user would be
			// able to modify settings of maps, layers and the atlas itself
			// while the AtlasThread works on that atlas reference
			AtlasInterface atlasToCreate = jAtlasTree.getAtlas().deepClone();
			Thread atlasThread = new AtlasThread(atlasToCreate);
			atlasThread.start();
		}
		catch (AtlasTestException e)
		{
			JOptionPane.showMessageDialog(null, "<html>" + e.getMessage() + "</html>", I18nUtils.localizedStringForKey("msg_convert_incompatible_format"),
					JOptionPane.ERROR_MESSAGE);

		}
	}

}
