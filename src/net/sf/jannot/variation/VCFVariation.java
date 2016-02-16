package net.sf.jannot.variation;

import java.util.HashMap;

import net.sf.jannot.tabix.TabixLine;

class VCFVariation implements Variation{

		private TabixLine line;

		public VCFVariation(TabixLine line) {
			this.line=line;
		}

		
		@Override
		public Allele[] alleles() {
			String ref=line.get(3);
			String alt=line.get(4);
//			String[]arr=line.get(7).split(";");
//			HashMap<String,String>map=map(arr);
			return new Allele[]{new Allele(ref,alt)};
		}

//		private HashMap<String, String> map(String[] arr) {
//			HashMap<String,String>out=new HashMap<String, String>();
//			for(String s:arr){
//				String[]split=s.split("=");
//				out.put(split[0],split[1]);
//			}
//			return out;
//		}

		@Override
		public int start() {
			return line.beg;
		}
		
	}