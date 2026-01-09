package net.sf.genomeview.gui.explorer;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JTextField;

import net.sf.genomeview.gui.explorer.FilteredListModel.Filter;
/**
 * 
 * @author Thomas Abeel
 *
 */
public class FilterField extends JTextField {

	private static final long serialVersionUID = -4782553201915780943L;

	private Filter all = new Filter() {

		@Override
		public boolean accept(Object element) {
			return true;
		}

	};

	class PartialStringFilter implements Filter {

		private String filter = null;

		public PartialStringFilter(String filter) {
			this.filter = filter;
		}

		@Override
		public boolean accept(Object element) {
			return element.toString().toLowerCase().contains(filter.toLowerCase());
		}

	}

	public FilterField(final FilteredListModel<String> flm){
		
		final JTextField _this=this;
		
		addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent e) {
				if(_this.getText().trim().length()==0){
					flm.setFilter(all);
				}else{
					flm.setFilter(new PartialStringFilter(_this.getText().trim()));
				}
				
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		addFocusListener(new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void focusGained(FocusEvent e) {
				_this.selectAll();
				
			}
		});
		
		addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				_this.selectAll();
				
			}
		});
		
	}

}
