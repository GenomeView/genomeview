package net.sf.genomeview.data;

import net.sf.jannot.EntrySet;
import net.sf.jannot.exception.ReadFailedException;
import net.sf.jannot.source.DataSource;
/**
 * 
 * @author Thomas Abeel
 *
 */
class TDataSource extends DataSource {

		protected TDataSource() {
			super(null);
			// TODO Auto-generated constructor stub
		}

		@Override
		public EntrySet read(EntrySet add) throws ReadFailedException {
			for (int i = 0; i < 5; i++)
				add.getOrCreateEntry("entry" + i);
			return add;
		}

		@Override
		public void finalize() {
			// TODO Auto-generated method stub

		}

		@Override
		public boolean isIndexed() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public long size() {
			// TODO Auto-generated method stub
			return 0;
		}

	

	}