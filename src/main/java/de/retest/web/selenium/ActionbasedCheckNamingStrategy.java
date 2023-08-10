package de.retest.web.selenium;

import java.util.Arrays;

import org.openqa.selenium.WebElement;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

/**
 * This class aims to create semantic names for checks based on the previously executed action that led to that state.
 * This looks like "enter_[my text]_into_user" or "click_contact". If a name occurs multiple times, the second and
 * following occasions are labeled with "_2", "_3" etc. The drawback of this naming strategy is, that if e.g. the target
 * changes, the state will not be recognized anymore. So it is recommended to use this naming strategy <em>only</em> in
 * combination with the <code>retestId</code>.
 */
public class ActionbasedCheckNamingStrategy implements AutocheckingCheckNamingStrategy {

	private static final int MAX_TEXT_INPUT_LENGTH = 10;

	private final Multiset<String> checks = HashMultiset.create();

	@Override
	public String getUniqueCheckName( final String action, final WebElement targetElement, final Object... params ) {
		String result = action;
		// "enter_[" + normalizeAndShorten( keysToSend ) + "]_into"
		if ( "enter".equals( action ) ) {
			// TODO Call FileUtils.normalize
			if ( params instanceof CharSequence[] ) {
				result = "enter_" + shortenTextInput( (CharSequence[]) params ) + "_into";
			} else {
				result = "enter_nothing_into";
			}
		}
		if ( "get".equals( action ) && params.length > 0 ) {
			// TODO Call FileUtils.normalize
			result = "get_[" + shortenUrl( params[0] ) + "]";
		}
		if ( targetElement != null ) {
			result = result + "_" + toString( targetElement );
		}
		return makeUnique( result );
	}

	@Override
	public String getUniqueCheckName( final String action ) {
		return makeUnique( action );
	}

	protected String makeUnique( final String result ) {
		checks.add( result );
		if ( checks.count( result ) == 1 ) {
			return result;
		}
		return result + "_" + checks.count( result );
	}

	protected String toString( final WebElement targetElement ) {
		String result = targetElement.toString();
		if ( result.contains( "->" ) ) {
			// remove driver info
			result = result.substring( result.lastIndexOf( "->" ) + 2 ).trim();
			// remove trailing ]
			result = result.substring( 0, result.length() - 1 );
			// remove identification criterion (e.g. id, class, ...)
			result = result.substring( result.lastIndexOf( ':' ) + 1 ).trim();
		}
		return result;
	}

	protected String shortenTextInput( final CharSequence[] keysToSend ) {
		if ( keysToSend == null || keysToSend.length == 0 ) {
			// How to properly represent empty string?
			return "";
		}
		final String stringToSend = Arrays.toString( keysToSend );
		if ( stringToSend.length() <= MAX_TEXT_INPUT_LENGTH ) {
			return stringToSend;
		}
		final String suffix = "...]";
		return stringToSend.substring( 0, MAX_TEXT_INPUT_LENGTH - suffix.length() ) + suffix;
	}

	protected String shortenUrl( final Object url ) {
		if ( url == null ) {
			return "";
		}
		return url.toString().replace( "http://", "" ).replace( "https://", "" );
	}

	@Override
	public void nextTest() {
		checks.clear();
	}
}
