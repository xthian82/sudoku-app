package py.com.snowtech.sudoku;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

public class Game extends Activity {

	public static final String KEY_DIFFICULTY = "py.com.snowtech.sudoku.difficulty";
	public static final int MAX_NUMBERS = 9;
	public static final int DIFFICULTY_EASY = 0;
	public static final int DIFFICULTY_MEDIUM = 1;
	public static final int DIFFICULTY_HARD = 2;
	private static final String TAG = "Game";
	private final String easyPuzzle = 
		"360000000004230800000004200" + 
		"070460003820000014500013020" + 
		"001900000007048300000000045"; 
	private final String mediumPuzzle = 
		"650000070000506000014000005" + 
		"007009000002314700000700800" + 
		"500000630000201000030000097"; 
	private final String hardPuzzle = 
		"009000000080605020501078000" + 
		"000000700706040102004000000" + 
		"000720903090301080000000600";
	
	private int puzzle[] = new int [MAX_NUMBERS * MAX_NUMBERS];
	private PuzzleView puzzleview;
	private int[][][] used = new int [MAX_NUMBERS][MAX_NUMBERS][];
	private static final String PREF_PUZZLE = "puzzle";
	protected static final int DIFFICULTY_CONTINUE = -1;
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate");
		getIntent().putExtra(KEY_DIFFICULTY, DIFFICULTY_CONTINUE);
		
		int diff = getIntent().getIntExtra(KEY_DIFFICULTY, DIFFICULTY_EASY);
		puzzle = getPuzzle(diff);
		calculateUsedTiles();
		
		puzzleview = new PuzzleView(this);
		setContentView(puzzleview);
		puzzleview.requestFocus();
		
		
		
	}

	private void calculateUsedTiles() {
		for (int x=0; x<MAX_NUMBERS; x++) {
			for (int y=0; y<MAX_NUMBERS; y++) {
				used[x][y] = calculateUsedTiles(x, y);
				//Log.d(TAG, "used[" + x +"][" + y + "]=" + toPuzzleString(used[x][y]));
			}
		}
	}

	private int[] calculateUsedTiles(int x, int y) {
		int i;
		int c[] = new int[MAX_NUMBERS];
		
		//horizontal
		for (i=0; i<MAX_NUMBERS;i++) {
			if (i == y) continue;
			
			int t = getTile(x, i);
			
			if (t != 0) c[t - 1] = t;
		}
		//vertical
		for (i=0; i<MAX_NUMBERS; i++) {
			if (i == x) continue;
			
			int t = getTile(i, y);
			
			if (t != 0) c[t - 1] = t;
		}
		//same cell block
		int startx = (x / 3) * 3;
		int starty = (y / 3) * 3;
		for (i=startx; i<startx + 3; i++) {
			for (int j=starty; j<starty + 3; j++) {
				if (x == i && j == y) continue;
				int t = getTile(i, j);
				if (t != 0)
					c[t - 1] = t;
			}
		}
		//compress
		int nused = 0;
		for (int t : c) {
			if (t != 0) ++nused;
		}
		
		int[] c1 = new int[nused];
		
		nused = 0;
		for (int t : c) {
			if (t != 0) c1[nused++] = t;
		}
		
		return c1;
	}

	private void setTile(int x, int y, int value) {
		puzzle[y * MAX_NUMBERS + x] = value;
	}
	
	private int getTile(int x, int y) {
		return puzzle[y * MAX_NUMBERS + x];
	}
	
	protected String getTileString(int x, int y) {
		int v = getTile(x, y);
		if (v == 0)
			return "";
		else
			return String.valueOf(v);
	}

	private int[] getPuzzle(int diff) {
		String puz;
		
		switch(diff) {
		case DIFFICULTY_CONTINUE:
			puz = getPreferences(MODE_PRIVATE).getString(PREF_PUZZLE, easyPuzzle);
			break;
		case DIFFICULTY_EASY:
			puz = easyPuzzle;
			break;
		case DIFFICULTY_MEDIUM:
			puz = mediumPuzzle;
			break;
		case DIFFICULTY_HARD:
			puz = hardPuzzle;
			break;
		default:
			puz = easyPuzzle;
			break;
		}
		
		return fromPuzzleString(puz);
	}

	static private String toPuzzleString(int[] puz) {
		StringBuilder buf = new StringBuilder();
		
		for (int element : puz) {
			buf.append(element);
		}
		
		return buf.toString();
	}
	
	static protected int[] fromPuzzleString(String str) {
		int [] puzz = new int [str.length()];
		
		for (int i=0; i<str.length(); i++) {
			puzz[i] = str.charAt(i) - '0';
		}
		
		return puzz;
	}

	public void showKeypadOrError(int x, int y) {
		int tiles[ ] = getUsedTiles(x, y);
		if (tiles.length == MAX_NUMBERS) {
			Toast toast = Toast.makeText(this, 
					R.string.no_moves_label, Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
		}
		else {
			Log.d(TAG, "showKeypad: used=" + toPuzzleString(tiles));
			Dialog v = new Keypad(this, tiles, puzzleview);
			v.show();
		}
	}

	protected boolean setTileIfValid(int x, int y, int value) {
		int tiles [] = getUsedTiles(x, y);
		if (value != 0) {
			for (int tile : tiles)
				if (tile == value)
					return false;
		}
		setTile(x, y, value);
		calculateUsedTiles();
		return true;
	}

	public int[] getUsedTiles(int i, int j) {
		return used [i][j];
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Music.play(this, R.raw.game);
	}
	
	protected void onPause() {
		super.onPause();
		Music.stop(this);
		
		//save the current puzzle
		getPreferences(MODE_PRIVATE).edit().putString(PREF_PUZZLE, toPuzzleString(puzzle)).commit();
	}
}
