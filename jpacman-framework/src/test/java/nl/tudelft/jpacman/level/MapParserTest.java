package nl.tudelft.jpacman.level;

import nl.tudelft.jpacman.PacmanConfigurationException;
import nl.tudelft.jpacman.board.Board;
import nl.tudelft.jpacman.board.BoardFactory;
import nl.tudelft.jpacman.board.Square;
import nl.tudelft.jpacman.npc.Ghost;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class MapParserTest {
    LevelFactory mockLevelFactory;
    BoardFactory mockBoardFactory;
    MapParser mapParser;

    @BeforeEach
    void init() {
        mockLevelFactory = mock(LevelFactory.class);
        mockBoardFactory = mock(BoardFactory.class);
        mapParser = new MapParser(mockLevelFactory, mockBoardFactory);
    }

    @Test
    void checkMapFormatWithNullTextTest() {
        assertThrows(PacmanConfigurationException.class, () ->
            mapParser.checkMapFormat(null)
        );
    }

    @Test
    void checkMapFormatWithEmptyArrayTest() {
        assertThrows(PacmanConfigurationException.class, () -> mapParser.checkMapFormat(new ArrayList<>()));
    }

    @Test
    void checkMapFormatWithEmptyTextFromArrayTest() {
        List<String> textList = new ArrayList<>();
        textList.add("");
        assertThrows(PacmanConfigurationException.class, () -> mapParser.checkMapFormat(textList));
    }

    @Test
    void makeGhostSquareBasicTest() {
        Square ground = mock(Square.class);
        Ghost ghost = mock(Ghost.class);
        List<Ghost> ghosts = new ArrayList<>();
        when(mockBoardFactory.createGround()).thenReturn(ground);

        Square result = mapParser.makeGhostSquare(ghosts, ghost);

        verify(mockBoardFactory).createGround();
        verify(ghost).occupy(ground);

        assertEquals(ground, result);
        assertThat(ghosts).containsExactly(ghost);
    }

    @Test
    void parseMapWithOnePlayerAndGhostTest() {
        Square wall = mock(Square.class);
        Square playerSquare = mock(Square.class);
        Square ghostSquare = mock(Square.class);

        Ghost ghost = mock(Ghost.class);
        Board board = mock(Board.class);
        Level level = mock(Level.class);

        when(mockBoardFactory.createWall()).thenReturn(wall);
        when(mockBoardFactory.createGround()).thenReturn(playerSquare, ghostSquare);
        when(mockLevelFactory.createGhost()).thenReturn(ghost);
        when(mockBoardFactory.createBoard(any(Square[][].class))).thenReturn(board);
        when(mockLevelFactory.createLevel(board, List.of(ghost), List.of(playerSquare))).thenReturn(level);

        /* Visual:
         * ##
         * PG
         */
        char[][] grid = {
            {'#', 'P'},
            {'#', 'G'}
        };

        Level result = mapParser.parseMap(grid);

        assertEquals(level, result);
        verify(mockBoardFactory, times(2)).createWall();
        verify(mockBoardFactory, times(2)).createGround();
        verify(mockLevelFactory).createGhost();
        verify(ghost).occupy(ghostSquare);
        verify(mockBoardFactory).createBoard(any(Square[][].class));
        verify(mockLevelFactory).createLevel(board, List.of(ghost), List.of(playerSquare));
    }

    @Test
    void parseMapWithOnePelletTest() {
        Square pelletSquare = mock(Square.class);
        Pellet pellet = mock(Pellet.class);

        Board board = mock(Board.class);
        Level level = mock(Level.class);

        when(mockBoardFactory.createGround()).thenReturn(pelletSquare);
        when(mockLevelFactory.createPellet()).thenReturn(pellet);
        when(mockBoardFactory.createBoard(any(Square[][].class))).thenReturn(board);
        when(mockLevelFactory.createLevel(board, List.of(), List.of())).thenReturn(level);

        char[][] grid = {
            {'.'}
        };

        Level result = mapParser.parseMap(grid);

        ArgumentCaptor<Square[][]> captor = ArgumentCaptor.forClass(Square[][].class);

        assertEquals(level, result);

        verify(mockBoardFactory).createGround();
        verify(mockLevelFactory).createPellet();
        verify(pellet).occupy(pelletSquare);
        verify(mockBoardFactory).createBoard(captor.capture());
        Square[][] actualGrid = captor.getValue();
        Square[][] expectedGrid = {{pelletSquare}};
        assertArrayEquals(expectedGrid, actualGrid);
        verify(mockLevelFactory).createLevel(board, List.of(), List.of());
    }

    @Test
    void parseMapRejectsUnknownCharacterTest() {
        PacmanConfigurationException exception = assertThrows(PacmanConfigurationException.class, () -> mapParser.parseMap(new char[][] {{'?'}}));

        assertThat(exception.getMessage()).contains("0,0", "?");
        verify(mockBoardFactory, never()).createBoard(any(Square[][].class));
        verify(mockLevelFactory, never()).createLevel(any(), anyList(), anyList());
    }
}
