package com.chayan.mulewa.chess.ui;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import com.chayan.mulewa.nframework.client.*;
import com.chayan.mulewa.nframework.common.exceptions.*;

public class ChessForClient extends JFrame
{
    NFrameworkClient client;
    JButton[][] squares;
    int selectedRow = -1;
    int selectedCol = -1;
    boolean isWhiteTurn = true;

    public ChessForClient() 
    {
        client=new NFrameworkClient();
        squares = new JButton[8][8];
        setName("Chess Client");
        setSize(600, 600);
        setLocation(100, 130);
        setVisible(true);
        setLayout(new GridLayout(8, 8));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setUpChessBoard();
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
                button.addActionListener(new ChessButtonActionListener(this,row,col));
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
        // selectedRow=-1;
        // selectedCol=-1;
        isWhiteTurn=!isWhiteTurn;
    }

    void sendMoveToServer(int selectedRow,int selectedCol,int targetRow, int targetCol)
    {
        Object []arg={selectedRow,selectedCol,targetRow,targetCol};
        try
        {
            client.execute("/chess/move",arg);
        }
        catch(Throwable ne)
        {
            System.out.println("sendMoveToServer : "+ne.getMessage());
        }
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

class PawnMoveValidator {
    ChessForClient c;
    int targetRow, targetCol;

    public PawnMoveValidator(ChessForClient c, int targetRow, int targetCol) {
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
            c.sendMoveToServer(c.selectedRow, c.selectedCol, targetRow, targetCol);
        } else if (c.selectedRow + 2 * direction == targetRow && c.selectedCol == targetCol && c.selectedRow == initialRow && c.squares[targetRow][targetCol].getIcon() == null) {
            c.movePiece(targetRow, targetCol);
            c.sendMoveToServer(c.selectedRow, c.selectedCol, targetRow, targetCol);
        }
        // Enforce pawn's capture rule: diagonal movement to capture opponent's piece.
        else if (c.selectedRow + direction == targetRow && Math.abs(c.selectedCol - targetCol) == 1 && c.squares[targetRow][targetCol].getIcon() != null) {
            c.movePiece(targetRow, targetCol);
            c.sendMoveToServer(c.selectedRow, c.selectedCol, targetRow, targetCol);
        }
    }
}

class RookMoveValidator
{
    ChessForClient c;
    int targetRow, targetCol;

    public RookMoveValidator(ChessForClient c, int targetRow, int targetCol)
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
                c.sendMoveToServer(c.selectedRow, c.selectedCol, targetRow, targetCol);
            }
        }
    }
}

class KnightMoveValidator
{
    ChessForClient c;
    int targetRow, targetCol;

    public KnightMoveValidator(ChessForClient c, int targetRow, int targetCol)
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
            c.sendMoveToServer(c.selectedRow, c.selectedCol, targetRow, targetCol);
        }
    }
}

class BishopMoveValidator
{
    ChessForClient c;
    int targetRow, targetCol;

    public BishopMoveValidator(ChessForClient c, int targetRow, int targetCol)
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
                c.sendMoveToServer(c.selectedRow, c.selectedCol, targetRow, targetCol);
            }
        }
    }
}

class QueenMoveValidator
{
    ChessForClient c;
    int targetRow, targetCol;

    public QueenMoveValidator(ChessForClient c, int targetRow, int targetCol)
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
            c.sendMoveToServer(c.selectedRow, c.selectedCol, targetRow, targetCol);
        }
    }
}

class KingMoveValidator
{
    ChessForClient c;
    int targetRow, targetCol;

    public KingMoveValidator(ChessForClient c, int targetRow, int targetCol)
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
            c.sendMoveToServer(c.selectedRow, c.selectedCol, targetRow, targetCol);
        }
    }
}

class ChessButtonActionListener implements ActionListener
{
    private ChessForClient c;
    private int row;
    private int col;
    
    public ChessButtonActionListener(ChessForClient c, int row, int col)
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
                    PawnMoveValidator p=new PawnMoveValidator(c, row, col);
                    p.isValidMoveForPawn();
                }
                else
                {
                    
                    if(c.squares[c.selectedRow][c.selectedCol].getIcon().toString().contains("Rook"))
                    {
                        RookMoveValidator r=new RookMoveValidator(c, row, col);
                        r.isValidMoveForRook();
                    }
                    else
                    {
                        if(c.squares[c.selectedRow][c.selectedCol].getIcon().toString().contains("Knight"))
                        {
                            KnightMoveValidator k=new KnightMoveValidator(c, row, col);
                            k.isValidMoveForKnight();
                        }
                        else
                        {
                            if(c.squares[c.selectedRow][c.selectedCol].getIcon().toString().contains("Bishop"))
                            {
                                BishopMoveValidator b=new BishopMoveValidator(c, row, col);
                                b.isValidMoveForBishop();
                            }
                            else
                            {
                                if(c.squares[c.selectedRow][c.selectedCol].getIcon().toString().contains("Queen"))
                                {
                                    QueenMoveValidator q=new QueenMoveValidator(c, row, col);
                                    q.isValidMoveForQueen();
                                }
                                else
                                {
                                    if(c.squares[c.selectedRow][c.selectedCol].getIcon().toString().contains("King"))
                                    {
                                        KingMoveValidator kn=new KingMoveValidator(c, row, col);
                                        kn.isValidMoveForKing();
                                    }   
                                }
                            }
                        }
                    }
                }
                c.selectedRow=-1;
                c.selectedCol=-1;
            }
        }
    }
}
