package Model;

import Server.Exception.Player.*;
import java.util.List;

public class Shelf {
    private Tile[][] myShelf;

    public Shelf() {
        this.myShelf = new Tile[6][5];
    }


    public void insert(int n, List<Tile> tiles) throws ColumnNotValidException{
        int tmp = tiles.size();

        for(int i=0; i<tmp; i++){
            if(this.myShelf[6-i][n] != null ) throw new ColumnNotValidException();
        }
        for(int i=0; i<6 ; i++ ){
            if(this.myShelf[i][n] == null){
                this.myShelf[i][n] = tiles.get(1);
            }
        }
    }

    public boolean full(){
        for(int i=0; i<5; i++)
            if(this.myShelf[0][i] == null)
                return false;
        return true;
    }

    public int checkMaxTiles(){
        int count = 0;
        int max = 0;

        for(int j=0; j<5; j++){
            for(int i=0; i<6; i++){
                if(this.myShelf[i][j] == null ) count++;
            }
            if(count > max) max = count;
        }
        return max;
    }

    public Shelf getShelf(){
        return this;
    }

    public Tile[][] getMyShelf() {
        return myShelf;
    }
}
