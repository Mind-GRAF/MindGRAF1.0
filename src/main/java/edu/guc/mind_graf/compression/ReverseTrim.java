package edu.guc.mind_graf.compression;

public class ReverseTrim {
  public static void reverseTrim(BipartiteGraph g) {
    g.reverseGraph();
    Trim.trim(g);
    g.reverseGraph();
}
   

}