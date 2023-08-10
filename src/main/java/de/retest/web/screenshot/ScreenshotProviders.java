package de.retest.web.screenshot;

import static de.retest.recheck.ui.image.ImageUtils.extractScale;

import java.awt.image.BufferedImage;
import java.lang.reflect.Method;

import org.aeonbits.owner.Converter;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import de.retest.web.RecheckWebOptions;
import de.retest.web.RecheckWebProperties;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ScreenshotProviders {

	/**
	 * The default screenshot provider: {@link ViewportOnlyMinimalScreenshot}.
	 */
	public static final ScreenshotProvider DEFAULT = new ViewportOnlyMinimalScreenshot();
	/**
	 * The none screenshot provider: {@link NoScreenshot}.
	 */
	public static final ScreenshotProvider NONE = new NoScreenshot();

	public static class ScreenshotProviderConverter implements Converter<ScreenshotProvider> {

		@Override
		public ScreenshotProvider convert( final Method method, final String input ) {
			switch ( input ) {
				case "fullPage":
					return new FullPageScreenshot();
				case "viewportOnly":
					return new ViewportOnlyScreenshot();
				case "viewportOnlyMinimal":
					return new ViewportOnlyMinimalScreenshot();
				case "none":
					log.info( "ScreenshotProvider has been set to 'none' either via property "
							+ RecheckWebProperties.SCREENSHOT_PROVIDER_PROPERTY_KEY + " or via "
							+ RecheckWebOptions.class.getSimpleName() + ", will create NO screenshots." );
					return NONE;
				default:
					log.warn( "Unknown configured screenshot provider '{}'. Using default value 'viewportOnlyMinimal'.",
							input );
					return DEFAULT;
			}
		}

	}

	public static final int SCALE = extractScale();

	private ScreenshotProviders() {}

	public static BufferedImage shoot( final WebDriver driver, final WebElement element,
			final ScreenshotProvider screenshotProvider ) {
		try {
			final long startTime = System.currentTimeMillis();
			if ( element != null ) {
				return screenshotProvider.shoot( driver, element );
			}
			final BufferedImage result = screenshotProvider.shoot( driver );
			log.info( "Took {}ms to create the screenshot.", System.currentTimeMillis() - startTime );
			return result;
		} catch ( final Exception e ) {
			log.error( "Exception creating screenshot for check.", e );
			return null;
		}
	}

}
