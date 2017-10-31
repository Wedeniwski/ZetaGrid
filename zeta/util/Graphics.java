/*--
  This file is a part of ZetaGrid, a simple and secure Grid Computing
  kernel.

  Copyright (c) 2001-2004 Sebastian Wedeniwski.  All rights reserved.

  Use in source and binary forms, with or without modification,
  are permitted provided that the following conditions are met:

  1. The source code must retain the above copyright
     notice, this list of conditions and the following disclaimer.

  2. The origin of this software must not be misrepresented; you must 
     not claim that you wrote the original software.  If you plan to
     use this software in a product, please contact the author.

  3. Altered source versions must be plainly marked as such, and must
     not be misrepresented as being the original software. The author
     must be informed about these changes.

  4. The name of the author may not be used to endorse or promote 
     products derived from this software without specific prior written 
     permission.

  THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS
  OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
  GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

  This program is based on the work of:
     S. Wedeniwski
--*/

package zeta.util;

import java.awt.Color;
import java.awt.Image;
import java.awt.Label;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.image.ImageObserver;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *  The <code>Graphics</code> class contains advanced graphical functions.
 *
 *  @version 1.6.1, October 22, 2002
**/
public class Graphics {

  /**
   *  floodFill starts at a particular x and y coordinate and fills it, and all
   *  the surrounding pixels with a color.
   *  @param image Image
   *  @param coordinatesColor list of triples (Integer: staring x coordinate, Integer: staring y coordinate, Color: color for flood fill)
   *  @param notFillColor flood fill will not be performed if the starting point has this color
   *  @return image with filled coordinated and coordinatesColor contains the not filles coordinates
  **/
  public static Image floodFill(Image image, List coordinatesColor, Color notFillColor) throws InterruptedException {
    final int imageHeight = image.getHeight(null);
    final int imageWidth = image.getWidth(null);
    int[] pixels = new int[imageWidth*imageHeight];
    PixelGrabber pg = new PixelGrabber(image.getSource(), 0, 0, imageWidth, imageHeight, pixels, 0, imageWidth);
    pg.grabPixels();
    if ((pg.getStatus() & ImageObserver.ABORT) != 0) {
      throw new InterruptedException("ImageObserver.ABORT");
    }
    List notFilledCoordinatesColor = new ArrayList(coordinatesColor.size());
    int pixelsToCheck[] = new int[2*imageWidth*imageHeight];
    Iterator i = coordinatesColor.iterator();
    while (i.hasNext()) {
      Object[] o = (Object[])i.next();
      int x = ((Integer)o[0]).intValue();
      int y = ((Integer)o[1]).intValue();
      final int c = ((Color)o[2]).getRGB();
      if (pixels[y*imageWidth + x] == c || pixels[y*imageWidth + x] == notFillColor.getRGB()) {
        notFilledCoordinatesColor.add(o);
        continue;
      }
      final int startColor = pixels[y*imageWidth + x];
      pixels[y*imageWidth + x] = c;
      int pixelsToCheckSize = 2;
      pixelsToCheck[0] = x; pixelsToCheck[1] = y;
      while (pixelsToCheckSize > 0) {
        y = pixelsToCheck[--pixelsToCheckSize];
        x = pixelsToCheck[--pixelsToCheckSize];
        if (x > 0 && pixels[y*imageWidth + x-1] == startColor) {
          pixels[y*imageWidth + x-1] = c;
          pixelsToCheck[pixelsToCheckSize] = x-1;
          pixelsToCheck[pixelsToCheckSize+1] = y;
          pixelsToCheckSize += 2;
        }
        if (x < imageWidth-1 && pixels[y*imageWidth + x+1] == startColor) {
          pixels[y*imageWidth + x+1] = c;
          pixelsToCheck[pixelsToCheckSize] = x+1;
          pixelsToCheck[pixelsToCheckSize+1] = y;
          pixelsToCheckSize += 2;
        }
        if (y > 0 && pixels[(y-1)*imageWidth + x] == startColor) {
          pixels[(y-1)*imageWidth + x] = c;
          pixelsToCheck[pixelsToCheckSize] = x;
          pixelsToCheck[pixelsToCheckSize+1] = y-1;
          pixelsToCheckSize += 2;
        }
        if (y < imageHeight-1 && pixels[(y+1)*imageWidth + x] == startColor) {
          pixels[(y+1)*imageWidth + x] = c;
          pixelsToCheck[pixelsToCheckSize] = x;
          pixelsToCheck[pixelsToCheckSize+1] = y+1;
          pixelsToCheckSize += 2;
        }
      }
    }
    coordinatesColor.clear();
    i = notFilledCoordinatesColor.iterator();
    while (i.hasNext()) {
      coordinatesColor.add(i.next());
    }
    image = Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(imageWidth, imageHeight, pixels, 0, imageWidth));
    MediaTracker tracker = new MediaTracker(new Label());
    tracker.addImage(image, 0);
    tracker.waitForID(0);
    tracker.removeImage(image);
    return image;
  }
}
