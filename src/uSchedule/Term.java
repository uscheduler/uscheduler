package uSchedule;

/**
 * Created by psout on 3/29/2016.
 */
public class Term {
    int termNum;
    String termName;

    public void Term(){
        this.termNum = 0;
        this.termName = null;
    }
    public void Term(int i, String term){
        this.termName = term;
        this.termNum = i;
    }
    public void setTerm(int i, String term){
        this.termName = term;
        this.termNum = i;
    }
    public String toString(){
        return "Term[termNum=" + this.termNum + ", termName=" + this.termName + "]";
    }
}
