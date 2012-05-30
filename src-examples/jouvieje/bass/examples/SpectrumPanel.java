// BASS spectrum analyser example, copyright (c) 2002-2006 Ian Luck.

package jouvieje.bass.examples;

import static jouvieje.bass.Bass.*;
import static java.lang.Math.log10;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import static jouvieje.bass.defines.BASS_DATA.BASS_DATA_FFT2048;
import static jouvieje.bass.defines.BASS_DATA.BASS_DATA_FLOAT;
import static jouvieje.bass.utils.BufferUtils.newByteBuffer;
import static jouvieje.bass.utils.BufferUtils.SIZEOF_FLOAT;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import javax.swing.JPanel;

import jouvieje.bass.structures.BASS_CHANNELINFO;

/**
 * I've ported the C BASS example to NativeBass.
 * 
 * @author Jérôme Jouvie (Jouvieje)
 * 
 * WANT TO CONTACT ME ?
 * @author Jérôme Jouvie (Jouvieje)
 * @site   http://jerome.jouvie.free.fr/
 * @mail   jerome.jouvie@gmail.com
 */
public class SpectrumPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	/*
	 * Jouvieje note :
	 * ---------------
	 * To change display size, modify those values
	 */
	private int SPECTRUM_WIDTH = 512;	//Display width
	private int SPECTRUM_HEIGHT = 256;	//Height (changing requires palette adjustments too)

	public int BANDS = 28;
	private int specmode = 0, specpos = 0; //Spectrum mode (and marker pos for 2nd mode)

	private ByteBuffer buffer = null;
	private BufferedImage image = null;

	public SpectrumPanel(int width, int height) {
		SPECTRUM_WIDTH = width;
		SPECTRUM_HEIGHT = height;

		this.setSize(SPECTRUM_WIDTH, SPECTRUM_HEIGHT);
		this.setPreferredSize(new Dimension(SPECTRUM_WIDTH, SPECTRUM_HEIGHT));
		this.setMinimumSize(new Dimension(SPECTRUM_WIDTH, SPECTRUM_HEIGHT));
		this.setMaximumSize(new Dimension(SPECTRUM_WIDTH, SPECTRUM_HEIGHT));
		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				specmode = (specmode+1)%4;	//Swap spectrum mode
				reset3D();
			}
		});
		this.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				SPECTRUM_WIDTH = getWidth();
				SPECTRUM_HEIGHT = getHeight();
				reset3D();
			}
		});
	}
	/*
	 * If we toggle 3D or the spectrum panel has been rezised,
	 * update buffered image.
	 */
	private void reset3D() {
		if(specmode == 2) {
			specpos = 0;
			clearImage();
		}
	}

	/**
	 * Update the spectrum display
	 * @param chan channel (either HStream or HMUSIC)
	 */
	public void update(int chan) {
		//Draw spectrum offscreen
		int y = 0, y1 = 0;

		if(specmode == 3) {
			clearImage();

			// waveform
			BASS_CHANNELINFO ci = BASS_CHANNELINFO.allocate();
			BASS_ChannelGetInfo(chan, ci);					//Get number of channels
			final int channels = ci.getChannels();
			ci.release();

			int size = channels*SPECTRUM_WIDTH*SIZEOF_FLOAT;
			if(buffer == null || buffer.capacity() < size)
				buffer = newByteBuffer(size);	//Allocate buffer for data

			BASS_ChannelGetData(chan, buffer, size | BASS_DATA_FLOAT);		//Get the sample data (floating-point to avoid 8 & 16 bit processing)
			FloatBuffer floats = buffer.asFloatBuffer();

			for(int c = 0; c < channels; c++) {
				for(int x = 0; x < SPECTRUM_WIDTH; x++) {
					int v = (int)( (1-floats.get(x*channels+c))*SPECTRUM_HEIGHT/2 );	//Invert and scale to fit display
					if(v < 0) {
						v = 0;
					}
					else if(v >= SPECTRUM_HEIGHT) {
						v = SPECTRUM_HEIGHT-1;
					}

					if(x == 0) {
						y = v;
					}

					do {
						// draw line from previous sample...
						if(y < v) {
							y++;
						}
						else if(y > v) {
							y--;
						}
						image.setRGB(x, swapY(y), getIndexColor((c == 1) ? 127 : 1));	//left=green, right=red (could add more colours to palette for more chans)
					} while(y != v);
				}
			}
		}
		else {
			final int size = 1024 * SIZEOF_FLOAT;
			if(buffer == null || buffer.capacity() < size) {
				buffer = newByteBuffer(size);
			}

			BASS_ChannelGetData(chan, buffer, BASS_DATA_FFT2048);	//Get the FFT data
			FloatBuffer floats = buffer.asFloatBuffer();

			if(specmode == 0) {
				clearImage();

				//"normal" FFT
				for(int x = 0; x < SPECTRUM_WIDTH / 2; x++) {
					if(true) {
						y = (int)( sqrt(floats.get(x+1))*3*SPECTRUM_HEIGHT-4 );	//Scale it (sqrt to make low values more visible)
					}
					else {
						y = (int)( floats.get(x+1)*10*SPECTRUM_HEIGHT );			//Scale it (linearly)
					}

					if(y > SPECTRUM_HEIGHT) {
						y = SPECTRUM_HEIGHT;			//Cap it
					}
					if(x != 0 && (y1 = (y+y1)/2) != 0) {	//Interpolate from previous to make the display smoother
						while(--y1 >= 0) {
							image.setRGB(x*2-1, swapY(y1), getIndexColor(adjustIndex(y1+1)));
						}
					}
					y1 = y;
					while(--y >= 0) {
						image.setRGB(x*2, swapY(y), getIndexColor(adjustIndex(y+1)));	//Draw level
					}
				}
			}
			else if(specmode == 1) {
				clearImage();
				Graphics graphics = image.getGraphics();

				//Logarithmic, acumulate & average bins
				int b0 = 0;
				for(int x = 0; x < BANDS; x++) {
					int b1 = (int)pow(2, x*10.0/(BANDS-1));
					if(b1 > 1023) {
						b1 = 1023;
					}
					if(b1 <= b0) {
						b1 = b0+1;		//Make sure it uses at least 1 FFT bin
					}

					int sc = 10+b1-b0;

					float sum = 0;
					for(; b0 < b1; b0++) {
						sum += floats.get(1+b0);
					}

					y = (int)( (sqrt(sum/log10(sc))*1.7*SPECTRUM_HEIGHT)-4 );	//Scale it
					if(y > SPECTRUM_HEIGHT) {
						y = SPECTRUM_HEIGHT;				//Cap it
					}

					while(--y >= 0) {
						//Sraw bar
						int startEndY = swapY(y);
						int startX = x*(SPECTRUM_WIDTH/BANDS);
						int endX = startX + SPECTRUM_WIDTH/BANDS-3;
						graphics.setColor(new Color(getIndexColor(adjustIndex(y+1))));
						graphics.drawLine(startX, startEndY, endX, startEndY);
					}
				}
			}
			else {
				/*
				 * Jouvieje note :
				 * ---------------
				 * Draw the spectrum, with history, vertically.
				 */

				//"3D"
				for(int x = 0; x < SPECTRUM_HEIGHT; x++) {
					y = (int)(sqrt(floats.get(x+1))*3*127);	//Scale it (sqrt to make low values more visible)
					if(y > 127) {
						y = 127;	//Cap it
					}
					image.setRGB(specpos, swapY(x), getIndexColor(128+y));	//Plot it
				}
				//Move marker onto next position
				specpos = (specpos+1)%SPECTRUM_WIDTH;
				Graphics graphics = image.getGraphics();
				graphics.setColor(new Color(getIndexColor(255)));
				graphics.drawLine(specpos, 0, specpos, SPECTRUM_HEIGHT-1);
//				int c = getIndexColor(255);
//				for(int x = 0; x < SPECHEIGHT; x++)
//				image.setRGB(specpos, swapY(x), c);
			}
		}

		//Draw the spectrum on the screen
		getGraphics().drawImage(image, 0, 0, null);
	}

	private void clearImage() {
		image = new BufferedImage(SPECTRUM_WIDTH, SPECTRUM_HEIGHT, BufferedImage.TYPE_INT_RGB);
	}
	/**
	 * @param y
	 * @return y swap
	 */
	private int swapY(int y) {
		return y = SPECTRUM_HEIGHT-1-y;
	}

	/**
	 * Adjustment if the height was changed
	 */
	private int adjustIndex(int i) {
		if(SPECTRUM_HEIGHT != 127) {
			return i * 127 / SPECTRUM_HEIGHT;
		}
		return i;
	}
	/**
	 * Simulate a color index model
	 * @param i index 
	 * @return rgb color
	 */
	private int getIndexColor(int i) {
		if(i == 0) {
			return 0; //Black
		}
		else if(i == 255) {
			return (255 << 16) + (255 << 8) + 255; //White
		}
		else if(i < 128) {
			return ((2 * i) << 16) + ((256 - 2 * i) << 8); //Mix of rb
		}
		else if(i < 160) {
			i = i - 128;
			return 8 * i; //Blue
		}
		else if(i < 192) {
			i = i - 160;
			return ((8 * i) << 16) + 255; //Mix of rb
		}
		else if(i < 214) {
			i = i - 192;
			return ((255) << 16) + ((8 * i) << 8) + 8 * (31 - i); //Mix of rgb
		}
		else if(i < 255) {
			i = i - 214;
			return ((255) << 16) + (255 << 8) + 8 * i; //Mix of rgb
		}
		//Out of bounds !
		return 0;
	}
}
