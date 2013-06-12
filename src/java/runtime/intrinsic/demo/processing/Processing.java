/**
 * ADOBE SYSTEMS INCORPORATED
 * Copyright 2009-2013 Adobe Systems Incorporated
 * All Rights Reserved.
 *
 * NOTICE: Adobe permits you to use, modify, and distribute
 * this file in accordance with the terms of the MIT license,
 * a copy of which can be found in the LICENSE.txt file or at
 * http://opensource.org/licenses/MIT.
 */
package runtime.intrinsic.demo.processing;

import processing.core.PApplet;
import runtime.rep.Lambda;
import runtime.rep.map.MapValue;
import runtime.rep.Symbol;
import runtime.rep.Tuple;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Demo support, Processing hook
 *
 * @author Basil Hosmer
 */
@SuppressWarnings("serial")
public final class Processing extends PApplet
{
    static Processing INSTANCE = null;

    static boolean isOpen()
    {
        return INSTANCE != null;
    }

    static synchronized void open(final String title, final MapValue methods)
    {
        if (!isOpen())
        {
            // Note: PROC is initialized in newInstance, so it's set when setup() runs
            newInstance(title, methods);
        }
    }

    static synchronized void close()
    {
        if (isOpen())
        {
            INSTANCE.stop();
            INSTANCE.frame.dispose();
            INSTANCE = null;
        }
    }

    /**
     * Stripped version of {@link PApplet#main}.
     *
     * @return new instance
     */
    static public Processing newInstance(final String title, final MapValue methods)
    {
        // Disable abyssmally slow Sun renderer on OS X 10.5.
        if (platform == MACOSX)
        {
            // Only run this on OS X otherwise it can cause a permissions error.
            // http://dev.processing.org/bugs/show_bug.cgi?id=976
            System.setProperty("apple.awt.graphics.UseQuartz", useQuartz);
        }

        Color backgroundColor = Color.BLACK;
        final GraphicsDevice displayDevice;

        final GraphicsEnvironment environment =
            GraphicsEnvironment.getLocalGraphicsEnvironment();
        displayDevice = environment.getDefaultScreenDevice();

        final Frame frame = new Frame(displayDevice.getDefaultConfiguration());

        // remove the grow box by default
        // users who want it back can call frame.setResizable(true)
        frame.setResizable(false);

        // Set the trimmings around the image
        final Image image = Toolkit.getDefaultToolkit().createImage(ICON_IMAGE);
        frame.setIconImage(image);
        frame.setTitle(title);

        final Processing applet = new Processing(methods);

        // these are needed before init/start
        applet.frame = frame;
        applet.args = new String[]{};

        frame.setLayout(null);
        frame.add(applet);
        frame.pack();

        //
        // initialize global processing window here, before setup() runs
        //
        INSTANCE = applet;

        applet.init();

        // Wait until the applet has figured out its width.
        // In a static mode app, this will be after setup() has completed,
        // and the empty draw() has set "finished" to true.
        // TODO make sure this won't hang if the applet has an exception.
        while (applet.defaultSize && !applet.finished)
        {
            //System.out.println("default size");
            try
            {
                Thread.sleep(5);

            }
            catch (InterruptedException e)
            {
                //System.out.println("interrupt");
            }
        }
        //println("not default size " + applet.width + " " + applet.height);
        //println("  (g width/height is " + applet.g.width + "x" + applet.g.height + ")");

        // can't do pack earlier cuz present mode don't like it
        // (can't go full screen with a frame after calling pack)
        //        frame.pack();  // get insets. get more.
        final Insets insets = frame.getInsets();

        final int windowW = Math.max(applet.width, MIN_WINDOW_WIDTH) +
            insets.left + insets.right;
        final int windowH = Math.max(applet.height, MIN_WINDOW_HEIGHT) +
            insets.top + insets.bottom;

        frame.setSize(windowW, windowH);

        if (backgroundColor == Color.black)
        {  //BLACK) {
            // this means no bg color unless specified
            backgroundColor = SystemColor.control;
        }
        frame.setBackground(backgroundColor);

        final int usableWindowH = windowH - insets.top - insets.bottom;
        applet.setBounds((windowW - applet.width) / 2,
            insets.top + (usableWindowH - applet.height) / 2,
            applet.width, applet.height);

        frame.addWindowListener(new WindowAdapter()
        {
            public void windowClosing(final WindowEvent e)
            {
                close();
            }
        });

        // handle frame resizing events
        applet.setupFrameResizeListener();

        // all set for rockin
        if (applet.displayable())
        {
            frame.setVisible(true);
        }

        applet.requestFocus(); // ask for keydowns
        //System.out.println("exiting main()");

        return applet;
    }

    // ---

    private final Lambda _draw;
    private final Lambda _keyTyped;
    private final Lambda _keyPressed;
    private final Lambda _keyReleased;
    private final Lambda _mouseMoved;
    private final Lambda _mouseClicked;
    private final Lambda _mouseDragged;
    private final Lambda _mousePressed;
    private final Lambda _mouseReleased;
    private final Lambda _setup;

    public Processing(final MapValue methods)
    {
        this._draw = getMethod(methods, "draw");
        this._keyTyped = getMethod(methods, "keyTyped");
        this._keyPressed = getMethod(methods, "keyPressed");
        this._keyReleased = getMethod(methods, "keyReleased");
        this._mouseClicked = getMethod(methods, "mouseClicked");
        this._mouseDragged = getMethod(methods, "mouseDragged");
        this._mouseMoved = getMethod(methods, "mouseMoved");
        this._mousePressed = getMethod(methods, "mousePressed");
        this._mouseReleased = getMethod(methods, "mouseReleased");
        this._setup = getMethod(methods, "setup");
    }

    public void draw()
    {
        if (_draw != null)
            _draw.apply(Tuple.UNIT);
    }

    public void keyTyped()
    {
        if (_keyTyped != null)
            _keyTyped.apply(Tuple.UNIT);
    }

    public void keyPressed()
    {
        if (_keyPressed != null)
            _keyPressed.apply(Tuple.UNIT);
    }

    public void keyReleased()
    {
        if (_keyReleased != null)
            _keyReleased.apply(Tuple.UNIT);
    }

    public void mouseClicked()
    {
        if (_mouseClicked != null)
            _mouseClicked.apply(Tuple.UNIT);
    }

    public void mouseDragged()
    {
        if (_mouseDragged != null)
            _mouseDragged.apply(Tuple.UNIT);
    }

    public void mouseMoved()
    {
        if (_mouseMoved != null)
            _mouseMoved.apply(Tuple.UNIT);
    }

    public void mousePressed()
    {
        if (_mousePressed != null)
            _mousePressed.apply(Tuple.UNIT);
    }

    public void mouseReleased()
    {
        if (_mouseReleased != null)
            _mouseReleased.apply(Tuple.UNIT);
    }

    public void setup()
    {
        frameRate(60);

        if (_setup != null)
            _setup.apply(Tuple.UNIT);
    }

    // helper

    private static Lambda getMethod(final MapValue map, final String name)
    {
        return (Lambda)map.get(Symbol.get(name));
    }
}
