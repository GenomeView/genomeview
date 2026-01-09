/**
 * %HEADER%
 */
package net.sf.genomeview.core;

import java.awt.Color;
import java.lang.reflect.Field;

/**
 * Utility class to work with colors.
 * 
 * @author Thomas Abeel
 * 
 */
public class Colors {

	
	public static final Color LIGHEST_GRAY=new Color(230, 230, 230);
    /**
     * Make a string encoding of the color
     * 
     * @param c
     *            the color to encode
     * @return a string representation for the color
     */
    public static String encode(Color c) {
        //return "RGB(" + c.getRed() +","+ c.getGreen()+"," + c.getBlue() + ")";
    	String rgb = Integer.toHexString(c.getRGB());
    	rgb = rgb.substring(2, rgb.length());
    	return "#"+rgb;

    }

    private static Color checkedColor(int r, int g, int b) {
        r = r < 0 ? 0 : r;
        g = g < 0 ? 0 : g;
        b = b < 0 ? 0 : b;

        r = r > 255 ? 255 : r;
        g = g > 255 ? 255 : g;
        b = b > 255 ? 255 : b;

        return new Color(r, g, b);
    }

    public static Color getColorCoding(double value) {
        double average1 = 0.33333;
        double average2 = 0.66667;
        if (value > average2) {// red-yellow
            value -= average2;
            value /= 1 - average2;
            return checkedColor(255, 255 - (int) (value * 255), 0);
        } else if (value > average1) {// yellow-green
            value -= average1;
            value /= (average2 - average1);
            return checkedColor((int) (value * 255), 255, 0);
        } else {// green-blue
            value -= 0;
            value /= (average1 - 0);
            return checkedColor(0, (int) (value * 255), 255 - (int) (value * 255));
        }

    }

    /**
     * Takes a color String in the format of RGB(FFFFFF) or a common color name
     * (red, blue, gray, black, ...) (case insensitive) and returns the matching
     * <code>Color</code>
     * 
     * @param colorString
     *            string with RGB value or textual color. Will always return a
     *            color. If no matching color is found, returns gray.
     * @return the matching color object.
     */
    public static Color decodeColor(String colorString) {
        if(colorString==null)
            return Color.GRAY;
        Color color;
        try {
            if (colorString.startsWith("#")) {
                color = Color.decode(colorString);
            } else if (colorString.startsWith("RGB")) {
                // extract RGB value and create Color object
                String rgb = colorString.substring(colorString.indexOf('(')+1, colorString.indexOf(')'));
                String[]arr=rgb.split(",");
                color = new Color(Integer.parseInt(arr[0]),Integer.parseInt(arr[1]),Integer.parseInt(arr[2]));
            } else if(colorString.contains(",")) {
            	String[]arr=colorString.split(",");
                color = new Color(Integer.parseInt(arr[0]),Integer.parseInt(arr[1]),Integer.parseInt(arr[2]));
            }else{
                // if a color name is given, see if a Color constant is
                // defined, otherwise use gray.
                Field colorField = Color.class.getDeclaredField(colorString);
                color = (Color) colorField.get(Color.class);
            }
        } catch (NumberFormatException e) {
            // the method can fail is the text representation has no matching
            // color constant of if the RGB value isn't hexa-decimal, decimal or
            // octal.
            // in that case: be gray.
            color = Color.GRAY;
        } catch (NoSuchFieldException e) {
        	color = Color.GRAY;
		} catch (IllegalAccessException e) {
			color = Color.GRAY;
		}
        return color;
    }

    public static Color getShadeColor(Color shapeColor) {
        // TODO better measurement for darkness?
        if (shapeColor == Color.BLUE) {
            return Color.CYAN;
        } else if (shapeColor.getBlue() < 20 && shapeColor.getRed() < 50 && shapeColor.getGreen() < 50
                || (shapeColor.getBlue() > 180 && (shapeColor.getRed() < 100 && shapeColor.getGreen() < 100))) {
            return shapeColor.brighter().brighter().brighter().brighter().brighter().brighter();
        } else {
            return shapeColor.darker();
        }
    }

    public static Color getTextColor(Color shapeColor) {
        if (shapeColor == Color.BLUE) {
            return Color.WHITE;
        } else if (shapeColor.getBlue() < 20 && shapeColor.getRed() < 50 && shapeColor.getGreen() < 50
                || (shapeColor.getBlue() > 180 && (shapeColor.getRed() < 100 && shapeColor.getGreen() < 100))) {
            return Color.WHITE;
        } else {
            return Color.BLACK;
        }
    }

}
