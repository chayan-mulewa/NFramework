package com.chayan.mulewa.chess.ui;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import com.chayan.mulewa.nframework.server.annotations.*;
import com.chayan.mulewa.nframework.server.*;
@Path("/chess")
public class ChessForServer extends JFrame
{
    private static ChessForServer instance;
    NFrameworkServer server;
    JButton[][] squares;
    int selectedRow = -1;
    int selectedCol = -1;
    boolean isWhiteTurn = true;

    private ChessForServer() 
    {
        squares = new JButton[8][8];
        setName("Chess Server");
        setSize(600, 600);
        setLocation(800, 130);
        setVisible(true);
        setLayout(new GridLayout(8, 8));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setUpChessBoard();
    }

    public static ChessForServer getInstance()
    {
        if (instance == null)
        {
            instance = new ChessForServer();
        }
        return instance;
    }
    private void setUpChessBoard()
    {
        for(int row=0;row<8;row++)
        {
            for(int col=0;col<8;col++)
            {
                JButton button = new JButton();
                squares[row][col] = button;
                // Set the button's color based on the chess board pattern
                if((row+col)%2==0)
                {
                    button.setBackground(Color.decode("#eeeed2"));
                }
                else
                {
                    button.setBackground(Color.decode("#769655"));
                }
                // Add ActionListener to handle button click events
                button.addActionListener(new ChessButtonActionListenerForServer(ChessForServer.this,row,col));
                // Add the button to the chess board
                add(button);
                if(row==0 || row==7)
                {
                    addPiece(button, getInitialPiece(row,col));
                }
                if(row==1)
                {
                    addPiece(button,"Black Pawn");
                }
                else
                {
                    if (row==6)
                    {
                        addPiece(button,"White Pawn");
                    }
                } 
            }
        }
    }

    String getInitialPiece(int row,int col)
    {
        String[] whitePieces={"White Rook", "White Knight", "White Bishop", "White Queen", "White King", "White Bishop", "White Knight", "White Rook"};
        String[] blackPieces={"Black Rook", "Black Knight", "Black Bishop", "Black Queen", "Black King", "Black Bishop", "Black Knight", "Black Rook"};
        if(row==0)
        {
            return blackPieces[col];
        }
        else
        {
            return whitePieces[col];
        }
    }

    void addPiece(JButton button, String piece) 
    {
        ImageIcon imageIcon=new ImageIcon(this.getClass().getResource("/image/"+piece+".png"));
        button.setIcon(imageIcon);
    }

    void movePiece(int targetRow, int targetCol) 
    {
        JButton sourceButton=squares[selectedRow][selectedCol];
        JButton targetButton=squares[targetRow][targetCol];
        targetButton.setIcon(sourceButton.getIcon());
        sourceButton.setIcon(null);
        selectedRow=-1;
        selectedCol=-1;
        isWhiteTurn=!isWhiteTurn;
    }

    @Path("/move")
    public void processServerMove(int sourceRow, int sourceCol, int targetRow, int targetCol) {
    JButton sourceButton = squares[sourceRow][sourceCol];
    JButton targetButton = squares[targetRow][targetCol];

    // Get the icon from the source button
    Icon sourceIcon = sourceButton.getIcon();

    // If there is no piece on the source button, return as it's an invalid move
    if (sourceIcon == null) {
        return;
    }

    // Get the icon from the target button
    Icon targetIcon = targetButton.getIcon();

    // If there is a piece on the target button and it's of the same color as the source piece, return as it's an invalid move
    if (targetIcon != null && (isWhiteTurn && targetIcon.toString().contains("White") || !isWhiteTurn && targetIcon.toString().contains("Black"))) {
        return;
    }

    // Move the piece to the target button
    targetButton.setIcon(sourceIcon);
    sourceButton.setIcon(null);

    // Toggle the turn for the next move
    isWhiteTurn = !isWhiteTurn;
}

    boolean isPathObstructed(int targetRow, int targetCol)
    {
        int rowDirection = targetRow - selectedRow;
        int colDirection = targetCol - selectedCol;

        int rowIncrement = (rowDirection == 0) ? 0 : rowDirection / Math.abs(rowDirection);
        int colIncrement = (colDirection == 0) ? 0 : colDirection / Math.abs(colDirection);

        int currentRow = selectedRow + rowIncrement;
        int currentCol = selectedCol + colIncrement;

        while (currentRow != targetRow || currentCol != targetCol)
        {
            if (squares[currentRow][currentCol].getIcon() != null)
            {
                return true; // Path is obstructed by another piece.
            }
            currentRow += rowIncrement;
            currentCol += colIncrement;
        }
        return false; // Path is clear.
    }
}

class PawnMoveValidatorForServer {
    ChessForServer c;
    int targetRow, targetCol;

    public PawnMoveValidatorForServer(ChessForServer c, int targetRow, int targetCol) {
        this.c = c;
        this.targetRow = targetRow;
        this.targetCol = targetCol;
    }

    public void isValidMoveForPawn() {
        int direction = c.isWhiteTurn ? -1 : 1;
        int initialRow = c.isWhiteTurn ? 6 : 1; // Initial row for white (6) and black (1) pawns.

        // Enforce pawn's movement rules: one step forward or two steps forward on the first move.
        if (c.selectedRow + direction == targetRow && c.selectedCol == targetCol && c.squares[targetRow][targetCol].getIcon() == null) {
            c.movePiece(targetRow, targetCol);
        } else if (c.selectedRow + 2 * direction == targetRow && c.selectedCol == targetCol && c.selectedRow == initialRow && c.squares[targetRow][targetCol].getIcon() == null) {
            c.movePiece(targetRow, targetCol);
        }
        // Enforce pawn's capture rule: diagonal movement to capture opponent's piece.
        else if (c.selectedRow + direction == targetRow && Math.abs(c.selectedCol - targetCol) == 1 && c.squares[targetRow][targetCol].getIcon() != null) {
            c.movePiece(targetRow, targetCol);
        }
    }
}

class RookMoveValidatorForServer
{
    ChessForServer c;
    int targetRow, targetCol;

    public RookMoveValidatorForServer(ChessForServer c, int targetRow, int targetCol)
    {
        this.c = c;
        this.targetRow = targetRow;
        this.targetCol = targetCol;
    }

    public void isValidMoveForRook()
    {
        if (Math.abs(c.selectedRow - targetRow) == 0 && Math.abs(c.selectedCol - targetCol) != 0 || Math.abs(c.selectedRow - targetRow) != 0 && Math.abs(c.selectedCol - targetCol) == 0)
        {
            if (!c.isPathObstructed(targetRow, targetCol))
            {
                c.movePiece(targetRow, targetCol);
            }
        }
    }
}

class KnightMoveValidatorForServer
{
    ChessForServer c;
    int targetRow, targetCol;

    public KnightMoveValidatorForServer(ChessForServer c, int targetRow, int targetCol)
    {
        this.c = c;
        this.targetRow = targetRow;
        this.targetCol = targetCol;
    }

    public void isValidMoveForKnight()
    {
        if ((Math.abs(c.selectedRow - targetRow) == 2 && Math.abs(c.selectedCol - targetCol) == 1) || (Math.abs(c.selectedRow - targetRow) == 1 && Math.abs(c.selectedCol - targetCol) == 2))
        {
            c.movePiece(targetRow, targetCol);
        }
    }
}

class BishopMoveValidatorForServer
{
    ChessForServer c;
    int targetRow, targetCol;

    public BishopMoveValidatorForServer(ChessForServer c, int targetRow, int targetCol)
    {
        this.c = c;
        this.targetRow = targetRow;
        this.targetCol = targetCol;
    }

    public void isValidMoveForBishop()
    {
        if (Math.abs(c.selectedRow - targetRow) == Math.abs(c.selectedCol - targetCol))
        {
            if (!c.isPathObstructed(targetRow, targetCol))
            {
                c.movePiece(targetRow, targetCol);
            }
        }
    }
}

class QueenMoveValidatorForServer
{
    ChessForServer c;
    int targetRow, targetCol;

    public QueenMoveValidatorForServer(ChessForServer c, int targetRow, int targetCol)
    {
        this.c = c;
        this.targetRow = targetRow;
        this.targetCol = targetCol;
    }

    public void isValidMoveForQueen()
    {
        if ((Math.abs(c.selectedRow - targetRow) == Math.abs(c.selectedCol - targetCol) && !c.isPathObstructed(targetRow, targetCol)) || (Math.abs(c.selectedRow - targetRow) == 0 && Math.abs(c.selectedCol - targetCol) != 0) || (Math.abs(c.selectedRow - targetRow) != 0 && Math.abs(c.selectedCol - targetCol) == 0))
        {
            c.movePiece(targetRow, targetCol);
        }
    }
}

class KingMoveValidatorForServer
{
    ChessForServer c;
    int targetRow, targetCol;

    public KingMoveValidatorForServer(ChessForServer c, int targetRow, int targetCol)
    {
        this.c = c;
        this.targetRow = targetRow;
        this.targetCol = targetCol;
    }

    public void isValidMoveForKing()
    {
        if (Math.abs(c.selectedRow - targetRow) <= 1 && Math.abs(c.selectedCol - targetCol) <= 1)
        {
            c.movePiece(targetRow, targetCol);
        }
    }
}

class ChessButtonActionListenerForServer implements ActionListener
{
    private ChessForServer c;
    private int row;
    private int col;
    
    public ChessButtonActionListenerForServer(ChessForServer c, int row, int col)
    {
        this.c = c;
        this.row = row;
        this.col = col;
    }

    public void actionPerformed(ActionEvent e)
    {
        if ((c.isWhiteTurn && c.squares[row][col].getIcon() != null && c.squares[row][col].getIcon().toString().contains("White")) || (!c.isWhiteTurn && c.squares[row][col].getIcon() != null && c.squares[row][col].getIcon().toString().contains("Black")))
        {
            // Select the clicked piece
            c.selectedRow = row;
            c.selectedCol = col;
        } 
        else
        {
            if (c.selectedRow != -1 && c.selectedCol != -1)
            {
                if(c.squares[c.selectedRow][c.selectedCol].getIcon().toString().contains("Pawn"))
                {
                    PawnMoveValidatorForServer p=new PawnMoveValidatorForServer(c, row, col);
                    p.isValidMoveForPawn();
                }
                else
                {
                    
                    if(c.squares[c.selectedRow][c.selectedCol].getIcon().toString().contains("Rook"))
                    {
                        RookMoveValidatorForServer r=new RookMoveValidatorForServer(c, row, col);
                        r.isValidMoveForRook();
                    }
                    else
                    {
                        if(c.squares[c.selectedRow][c.selectedCol].getIcon().toString().contains("Knight"))
                        {
                            KnightMoveValidatorForServer k=new KnightMoveValidatorForServer(c, row, col);
                            k.isValidMoveForKnight();
                        }
                        else
                        {
                            if(c.squares[c.selectedRow][c.selectedCol].getIcon().toString().contains("Bishop"))
                            {
                                BishopMoveValidatorForServer b=new BishopMoveValidatorForServer(c, row, col);
                                b.isValidMoveForBishop();
                            }
                            else
                            {
                                if(c.squares[c.selectedRow][c.selectedCol].getIcon().toString().contains("Queen"))
                                {
                                    QueenMoveValidatorForServer q=new QueenMoveValidatorForServer(c, row, col);
                                    q.isValidMoveForQueen();
                                }
                                else
                                {
                                    if(c.squares[c.selectedRow][c.selectedCol].getIcon().toString().contains("King"))
                                    {
                                        KingMoveValidatorForServer kn=new KingMoveValidatorForServer(c, row, col);
                                        kn.isValidMoveForKing();
                                    }   
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
