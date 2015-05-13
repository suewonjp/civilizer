package com.civilizer.domain;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TextDecorator {
	
	public static final String PREFIX_FOR_HIGHLIGHT = "{{[sh]";
	public static final String POSTFIX_FOR_HIGHLIGHT = "}}";
	
	private static final RangeComparator rangeComparator = new RangeComparator();
	
	private static class RangeComparator implements Comparator<Pair<Integer, Integer>> {
		@Override
        public int compare(Pair<Integer, Integer> arg0, Pair<Integer, Integer> arg1) {
            int d = arg0.getFirst() - arg1.getFirst();
            if (d == 0) {
            	// in case that the two ranges overlap
            	// the bigger range will have higher priority
            	d = arg1.getSecond() - arg0.getSecond();
            }
            return d;
        }
	}
	
	private static String escapeRegexMetaCharacters(String input) {
		return "\\Q" + input + "\\E";
	}
	
	private static void match(List<Pair<Integer, Integer>> output, String input, SearchParams sp, boolean caseSensitive) {
		// Create a pattern from the search parameters
		Set<String> keywordSet = new HashSet<String>();

		for (SearchParams.Keywords keywords : sp.getKeywords()) {
		    for (SearchParams.Keyword kw : keywords.getWords()) {
		        final String w = kw.getWord();
		        if (kw.isId() || kw.isInverse()) {
		            continue;
		        }
		        if (caseSensitive) {
		            if (kw.isCaseSensitive()) {
						keywordSet.add(w);
		            }
		        }
		        else {
		            if (! kw.isCaseSensitive()) {
		                keywordSet.add(w.toLowerCase());
		            }
		        }
		    }
		}
		
		final int keywordCount = keywordSet.size();
		if (keywordCount == 0) {
			return;
		}
		
		String regex = "";
		String[] keywords = keywordSet.toArray(new String[keywordCount]);
		final int c = keywordCount - 1;
		for (int i=0; i<c; ++i) {
			regex += escapeRegexMetaCharacters(keywords[i]) + "|";
		}
		regex += escapeRegexMetaCharacters(keywords[keywords.length - 1]);
		final Pattern p = Pattern.compile(regex);
		
		// Apply regular expression with the pattern
		final String text = caseSensitive ? 
				input : input.toLowerCase();
		final Matcher m = p.matcher(text);
		
		// Populate the output ranges
		while (m.find()) {
			output.add(new Pair<Integer, Integer>(m.start(), m.end()));
		}
	}
	
	private static void matchUrls(List<Pair<Integer, Integer>> output, String input) {
		final Pattern p = Pattern.compile("https?://\\S+");
		final Matcher m = p.matcher(input.toLowerCase());
		
		// Populate the output ranges
		while (m.find()) {
			output.add(new Pair<Integer, Integer>(m.start(), m.end()));
		}
	}
	
	private static boolean rangeIsInsideUrl(Pair<Integer, Integer> range, List<Pair<Integer, Integer>> urlMatchRanges) {
		for (Pair<Integer, Integer> ur : urlMatchRanges) {
			if (ur.getFirst() <= range.getFirst() && range.getSecond() <= ur.getSecond()) {
				return true;
			}
		}
		return false;
	}
	
	public static String highlight(String input, SearchParams sp) {
		final List<Pair<Integer, Integer>> ranges = new ArrayList<Pair<Integer, Integer>>();
		
		// Each range (int, int pair) indicates a matched keyword;
		// We need two pass operations; case sensitive and case insensitive
		match(ranges, input, sp, false);
		match(ranges, input, sp, true);
		
		List<Pair<Integer, Integer>> urlMatchRanges = new ArrayList<Pair<Integer,Integer>>();
		matchUrls(urlMatchRanges, input);
		
		// Sort the ranges;
		// If not doing this, the resultant styling may get ugly
		Collections.sort(ranges, rangeComparator);
		
		String output = "";
		int pi = 0; // the end of the previous range
		for (Pair<Integer, Integer> r : ranges) {
			if (rangeIsInsideUrl(r, urlMatchRanges)) {
				// Ignore if the search phrase exists inside some URL patterns
				continue;
			}
			final int si = r.getFirst();
			if (si < pi) {
				// The range overlaps the previous range;
				// This case may be very rare, but we cannot guarantee it won't happen;
				// Just ignore it for now;
				continue;
			}
			final int ei = r.getSecond();
			output += input.substring(pi, si) + PREFIX_FOR_HIGHLIGHT + input.substring(si, ei) + POSTFIX_FOR_HIGHLIGHT;
			pi = ei;
		}		
		output += input.substring(pi, input.length());
		
		return pi == 0 ? input : output;
	}
	
}
