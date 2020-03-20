public class Hash {
	static final String[] Sec1 = new String[] {"a","w","c","k","e","f","g"};
	static final String[] Sec2 = new String[] {"h","p","j","d","z","t",};
	static final String[] Sec3 = new String[] {"o","i","q","y","l","m","u"};
	static final String[] Sec4 = new String[] {"v","b","x","r","s","n"};
	
	

	public static int hasher(String s) {
		int num = 1;
		for(String ex: Sec1) {
			if(s.substring(0, 1).toLowerCase().equals(ex)) {
				for (char x : s.toCharArray()) {
					num = ((int) x) * num;
				}
			return num % 250000;
			}
		}
		
		for(String ex: Sec2) {
			if(s.substring(0, 1).toLowerCase().equals(ex)) {
				for (char x : s.toCharArray()) {
					num = ((int) x) * num;
				}
			return (num % 250000) + 249999;
			}	
		}
		
		for(String ex: Sec3) {
			if(s.substring(0, 1).toLowerCase().equals(ex)) {
				for (char x : s.toCharArray()) {
					num = ((int) x) * num;
				}
			return (num % 250000) + 499999;
			}	
		}
		
		for(String ex: Sec4) {
			if(s.substring(0, 1).toLowerCase().equals(ex)) {
				for (char x : s.toCharArray()) {
					num = ((int) x) * num;
				}
			return (num % 250000) + 722801;
			}	
		}
		return 0;
		
	}
	
	

}
