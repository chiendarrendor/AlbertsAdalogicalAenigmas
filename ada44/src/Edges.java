/**
 * Created by chien on 6/9/2017.
 */
public enum Edges
{
    ISLAND(true),

    UNKNOWNLINK(false),
    LINK(true),
    NOTLINK(true),

    SLASH(true),
    BACKSLASH(true),
    NOTSLASH(false), // could be BS or BLOCK
    NOTBACKSLASH(false), // could be SLASH or BLOCK
    NOTANGLE(true),
    UNKNOWNANGLE(false);

    private boolean isterminal;
    Edges(boolean isterminal) { this.isterminal = isterminal;}
    public boolean isTerminal() { return isterminal; }

    // state diagram for diagonals:
    //
    //                  UNKNOWN
    //                   /     \
    //             NOTSLASH  NOTBACKSLASH
    //              /     \   /        \
    //     BACKSLASH     NOTANGLE     SLASH
    // (nicely enough, each stage of not knowing something leads to a binary guess!)

}
