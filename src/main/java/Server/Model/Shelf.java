package Server.Model;

import Exception.Player.ColumnNotValidException;

import java.util.List;

public class Shelf {
    private Tile[][] myShelf;

    public Shelf() {
        this.myShelf = new Tile[6][5];
    }

    public void insert(int n, List<Tile> tiles) throws ColumnNotValidException {
        int tmp = tiles.size();

        if(!(0<=n && n<=4) || this.myShelf[tiles.size()-1][n] != null )
            throw new ColumnNotValidException(n);
        for(int i=5; i>=0 ; i-- ){
            if(this.myShelf[i][n] == null){
                this.myShelf[i][n] = tiles.remove(0);
            }
        }
    }

    public boolean full(){
        for(int i=0; i<5; i++)
            if(this.myShelf[0][i] == null)
                return false;
        return false;
    }

    public int checkMaxTiles(){
        int count = 0;
        int max = 0;

        for(int j=0; j<5; j++){
            for(int i=0; i<6; i++){
                if(this.myShelf[i][j] == null ) 
                  count++;
                else break;
            }
            if(count > max) max = count;
        }
        return max;
    }

    public Tile getTile(int i, int j) {
        return myShelf[i][j];
    }

    public Tile[][] getMyShelf() {
        return myShelf;
    }

    public int checkEndGame() {
        return 0;
    }
}
