package g5;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Solution {
	
	public static void main(String [] args) {
		boolean[][] state = {
				{ true, true, false,  true, false,  true, false,  true,  true, false}, 
				{ true, true, false, false, false, false,  true,  true,  true, false}, 
				{ true, true, false, false, false, false, false, false, false,  true}, 
				{false, true, false, false, false, false,  true,  true, false, false}
				};
		
		int res = Solution.solution(state);
		System.out.printf("Expected: %d Actual: %d\n",11567, res);
		
//		for (int i = 3; i < 6; ++i) {
//			boolean[][] empty = new boolean[9][i];
//			System.out.println("Testing empty : " + 9 + "x" + i + " = " + solution(empty));
//		                
//		}
		
		
		
		for(int i =3; i < 51; ++i) {
			boolean[][] mx = prepareTask(i, 2, 1);
			System.out.printf("Testing mx[%d][%d] = %d\n", mx.length, mx[0].length, solution(mx));
		} 
		
		
	}

	
	
	
	private static boolean[][] prepareTask(int i, int skip, int set) {
		int rows = Math.min(i, 9);
		int cols = i;
		
		boolean[][] mx = new boolean[rows][cols];
		
		int setting = set;
		int skipping = 0;
		
		for (int r = 0; r < rows; r++) {
			boolean[] row = mx[r];
			
			
			for (int j = 0; j < row.length; j++) {
				if (setting > 0) {
					row[j] = true;
					if (--setting == 0)
						skipping = skip;
				}
				else {
					if (--skipping == 0)
						setting = set;
				}
				
				
			}
			
		}
		
		return mx;
	}




	public static int solution(boolean[][] state) {
		return new Solution().solve(state);
	}
	
	int rows, cols;
	boolean[][] state;
	int[] icols;   // contains int values of column bits
	int[][] pairs;  // contains results of a step for columns c1, c2
	int[][] cache;
	private Map<Integer, List<Pair>> pairsMap = new HashMap<>();

	void prepare(boolean[][] state) {
		this.rows = state.length;
		assert rows >= 3 && rows <= 9 : "rows >= 3 && rows <= 9, rows =" + rows ;
		this.cols = state[0].length;
		assert cols >= 3 && cols <= 50 : "cols >= 3 && cols <= 50, cols = " + cols;
		for(boolean[] row: state) {
			assert row.length == cols : "row.length == cols, cols = " + cols + " row.length = " + row.length;
		}
		System.out.printf("Rows:%d Cols: %d\n", rows, cols);
		this.icols = buildColumns(state, rows, cols);
		this.cache = new int[cols][1 << (rows + 1)]; 

		buildPairMatrix(rows, icols);
	}
	

	int solve(boolean[][] state) {
		
	   prepare(state);	
		
	   return f(0);
		
	}



	static class Pair{
		public Pair(int in, int out) {
			super();
			this.in = in;
			this.out = out;
		}

		final int in, out;
	}
	
	

	private int f(int col) {
		int total = 0;
		
		for(Pair p : pairs(0)) {
			total += f2(col + 1, p.out);
		}
		
		return total;
	}




	private Iterable<Pair> pairs(int col) {
		int tgt = icols[col];
		
		return this.pairsMap.get(tgt);
	}




	private int f2(int col, int out) {
		int c = cached(col, out);
		if (c >= 0)
			return c;
		int sum = 0;
		for (Pair p: pairs2(col, out)) {
			sum += f2(col + 1, p.out);
			assert sum < 1_000_000_000 : "sum = " + sum ;
		}	
		saveInCache(col, out, sum);
		return sum;
	}



	
	private Iterable<Pair> pairs2(int col, int out) {
		int tgt = icols[col];
		List<Pair>  list  = this.pairsMap.get(inKey(tgt, out));
		
		return list == null ?  Collections.<Pair>emptyList() : list;
	}




	private void saveInCache(int col, int out, int sum) {
		this.cache[col][out] = sum + 1;
		
	}

	private int cached(int col, int out) {
		if (col == this.cache.length)
			return 1;
		return this.cache[col][out] - 1;
	}








	private void buildPairMatrix(int rows, int[] icols) {
		
		for(int tgt: icols) {
			this.pairsMap.put(tgt, new ArrayList<Pair>());
		}
		
		int msize = 1 << rows + 1;
//		int[][] mx = new int[msize][msize];
		for (int c1 = 0; c1 < msize; c1++) {
			for (int c2 = 0; c2 < msize; c2++) {
				int afterStep = calcAfterStep(c1, c2, rows);
//				System.out.printf("c1 = %d, c2 = %d, a = %d\n", c1, c2, afterStep);
				List<Pair> list = pairsMap.get(afterStep);
				if (list != null) {
					Pair p = new Pair(c1, c2);
					list.add(p);
					int inKey = inKey(afterStep, c1);
					List<Pair> list2 = pairsMap.get(inKey);
					if (list2 == null) {
						list2 = new ArrayList<>();
						pairsMap.put(inKey, list2);
					}
					list2.add(p);
				}	
				
			}	
		}
		
	}




	private int inKey(int tgt, int c1) {
		
		return tgt | (c1 << 12) | (1 << 24) ;
	}




	private int calcAfterStep(int c1, int c2, int rows) {
		int after = 0;
		for(int r = 0; r < rows; ++r) {
			int bit1 = 1 << r;
			int bit2 = bit1 << 1;
			
			int sum = ((c1 & bit1) >> r)   +  ((c2 & bit1) >> r)    +  ((c1 & bit2) >> (r + 1))    +  ((c2 & bit2) >> (r + 1));
			if (sum == 1) {
				after |= 1 << r;
			}
		}
		
		return after;
	}




	private int[] buildColumns(boolean[][] state, int rows, int cols) {
	  	
		int[] res = new int[cols];
		
		for (int c = 0; c < cols; c++) {
			int i = 0;
			for (int r = 0; r < rows; r++) {
				if (state[r][c])
					i |= 1 << r;
			}
			res[c] = i;
			System.out.print(i + ", ");			
		}
		System.out.println();
	   return res;
	}
	
}
