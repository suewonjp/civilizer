package com.knowledgex.domain;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TextDecorator {
	
	private static final String PREFIX_HTML_TAG_FOR_HIGHLIGHT = "<span class=\"search-keyword\">";
	private static final String POSTFIX_HTML_TAG_FOR_HIGHLIGHT = "</span>";
	
	public static String highlight(String input, SearchParams sp) {
		// Create a pattern from the search parameters
		Set<String> keywordSet = new HashSet<String>();
		for (SearchParams.Keywords keywords : sp.getKeywords()) {
			for (SearchParams.Keyword kw : keywords.getWords()) {
				keywordSet.add(kw.getWord());
			}
		}
		String regex = "";
		String[] keywords = keywordSet.toArray(new String[keywordSet.size()]);
		final int c = keywordSet.size() - 1;
		for (int i=0; i<c; ++i) {
			regex += keywords[i] + "|";
		}
		regex += keywords[keywords.length - 1];
		final Pattern p = Pattern.compile(regex);
		
		// Apply regular expression with the pattern
		final Matcher m = p.matcher(input);
		
		String output = "";
		
		// Attach an HTML tag (i.e. <span class="search-keyword" />) to each matched text
		int pi = 0;
		while (m.find()) {
			final int si = m.start();
			final int ei = m.end();
			output += input.substring(pi, si) + PREFIX_HTML_TAG_FOR_HIGHLIGHT + input.substring(si, ei) + POSTFIX_HTML_TAG_FOR_HIGHLIGHT;
			pi = ei;
		}
		output += input.substring(pi, input.length());
		
		return pi == 0 ? input : output;
	}

}
