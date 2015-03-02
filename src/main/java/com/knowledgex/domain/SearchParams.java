package com.knowledgex.domain;

import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public final class SearchParams {
	
	public static final int TARGET_ALL     = 0;
	public static final int TARGET_TAG     = 1;
	public static final int TARGET_TITLE   = 2;
	public static final int TARGET_TEXT    = 3;
	public static final int TARGET_URL     = 4;
	
	public static final class Keyword {
		private final String word;
		private final boolean caseSensitive;
		private final boolean wholeWord;
		private final boolean asIs;
		
		public Keyword(String src) {
			Pattern p;
			Matcher m;
			String word = src.trim();
			boolean caseSensitive = false;
			boolean wholeWord = false;
			boolean asIs = false;
			
//			p = Pattern.compile("^'(([^']|'\\w)+)'");
//			m = p.matcher(src);
//			if (m.find()) {
			if (word.startsWith("'") && word.endsWith("'")) {
				// [RULE] '...' => as is, also becomes case sensitive and not whole word automatically
				asIs = true;
				caseSensitive = true;
//				word = m.group(1);
				word = word.substring(1, word.length()-1);
			}
			else {
				p = Pattern.compile("(.*)/([cw]+)");
				m = p.matcher(src);
				
				if (m.find()) {
					final String suffix = m.group(2);
					word = m.group(1);
					if (suffix.indexOf('c') != -1) {
						// [RULE] .../c => case sensitive
						caseSensitive = true;
					}
					if (suffix.indexOf('w') != -1) {
						// [RULE] .../w => whole word
						wholeWord = true;
					}
				}
				
				if (word.startsWith("\"") && word.endsWith("\"")) {
					// [RULE] if quoted with ", strip it
					word = word.substring(1, word.length()-1);
				}
			}
			
			this.word = word;
			this.caseSensitive = caseSensitive;
			this.wholeWord = wholeWord;
			this.asIs = asIs;
		}
		
		public boolean checkValidity() {
			return ! word.isEmpty();
		}

		public String getWord() {
			return word;
		}

		public boolean isCaseSensitive() {
			return caseSensitive;
		}

		public boolean isWholeWord() {
			return wholeWord;
		}

		public boolean isAsIs() {
			return asIs;
		}
	}
	
	public static final class Keywords {
		private final List<Keyword> words;
		private final int target;
		private final boolean any;
		
		public Keywords(List<Keyword> words, int target, boolean any) {
			this.words = words;
			this.target = target;
			this.any = any;
		}

		public List<Keyword> getWords() {
			return words;
		}

		public int getTarget() {
			return target;
		}

		public boolean isAny() {
			return any;
		}
	}
	
	private final List<Keywords> keywords;

	public SearchParams(List<Keywords> keywords) {
		this.keywords = keywords;
	}

	public List<Keywords> getKeywords() {
		return keywords;
	}

}
