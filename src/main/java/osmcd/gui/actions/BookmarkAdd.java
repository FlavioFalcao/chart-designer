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

import osmcd.gui.MainGUI;
import osmcd.gui.mapview.PreviewMap;
import osmcd.program.model.Bookmark;
import osmcd.program.model.Settings;
import osmcd.utilities.I18nUtils;

public class BookmarkAdd implements ActionListener
{

	private final PreviewMap previewMap;

	public BookmarkAdd(PreviewMap previewMap) {
		this.previewMap = previewMap;
	}

	public void actionPerformed(ActionEvent arg0)
	{
		Bookmark bm = previewMap.getPositionBookmark();
		String name = JOptionPane.showInputDialog(I18nUtils.localizedStringForKey("dlg_add_bookmark_msg"), bm.toString());
		if (name == null)
			return;
		bm.setName(name);
		Settings.getInstance().placeBookmarks.add(bm);
		MainGUI.getMainGUI().updateBookmarksMenu();
	}

}
