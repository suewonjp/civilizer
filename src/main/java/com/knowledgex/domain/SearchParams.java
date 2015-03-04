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
		
		public static Pair<String, Character> escapeSqlWildcardCharacters(String word) {
			// Escape SQL wildcards. ( '_' and '%')
			// If user provided words contain these characters, that means they are not intended on wildcards.
			// So escaping them is necessary before SQL treats them as wildcards
			
			final boolean hasUnderscore = (word.indexOf('_') != -1);
			final boolean hasPercent = (word.indexOf('%') != -1);
			char escapeChar = ' ';
			
			if (hasUnderscore || hasPercent) {
				for (int ascii=33; ascii<127; ++ascii) {
					if (ascii == '_' || ascii == '%' || ascii=='?' || ascii=='*'|| ascii=='\'' || ascii=='\"') {
						continue;
					}
					if (word.indexOf(ascii) == -1) {
						// The word doesn't contain this character, so we can safely use it as an escape character.
						escapeChar = (char) ascii;
						if (hasUnderscore) {
							word = word.replace("_", String.valueOf(escapeChar) + "_");
						}
						if (hasPercent) {
							word = word.replace("%", String.valueOf(escapeChar) + "%");
						}
						break;
					}
				}
			}
			
			return new Pair<String, Character>(word, escapeChar);
		}
		
		public static String translateToPatternForSqlLIKEClause(String word, boolean wholeWord, boolean asIs) {
//			final Pair<String, Character> tmp = escapeSqlWildcardCharacters(word);
//			word = tmp.getFirst();
			
			if (! asIs) {
				word = word.replace('?', '_').replace('*', '%');
				
				if (wholeWord) {
					// [TODO] The following pattern won't match a case when the text ends with the word and the word doesn't appear anywhere else.
					// We should take care of this edge case when we build the SQL query.
					// e.g. the final SQL should be like so:
					//    where text like '%[^a-z0-9_-]word[^a-z0-9_-]%' or like '%[^a-z0-9_-]word';
					final String boundary = "[^a-z0-9_-]";
					word = boundary + word + boundary;
				}
			}

			word = "%" + word + "%";
			
			return word;
		}
		
		private static boolean checkValidity(String word) {
			return ! word.isEmpty();
		}
		
		public boolean checkValidity() {
			return checkValidity(word);
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
	
	private static final class TargetDirective {
		public final String expression;
		public final int target;
		public final boolean any;
		
		public TargetDirective(String expr, int target, boolean any) {
			this.expression = expr.intern();
			this.target = target;
			this.any = any;
		}
	}
	
	public static final class Keywords {
		private final List<Keyword> words;
		private final int target;
		private final boolean any;
		
		public Keywords(String src) {
			src = src.trim();
			List<Keyword> words = new ArrayList<Keyword>();
			final TargetDirective targetDirective = parseTarget(src);
			int target = targetDirective.target;
			boolean any = targetDirective.any;
			final Pattern p = Pattern.compile("('([^']|'\\w)+')|(\"[^\"]+\")|(\\S+)");
			final Matcher m = p.matcher(src);
			
			while (m.find()) {
				words.add(new Keyword(m.group()));
			}
			
			if (words.isEmpty()) {
				words = Collections.emptyList();
			}
			
			this.words = words;
			this.target = target;
			this.any = any;
		}
		
		private static TargetDirective parseTarget(String src) {
			final TargetDirective[] directives = {
				new TargetDirective("tag:", TARGET_TAG, false),	
				new TargetDirective("anyintag:", TARGET_TAG, true),
				new TargetDirective("title:", TARGET_TITLE, false),	
				new TargetDirective("anyintitle:", TARGET_TITLE, true),
				new TargetDirective("text:", TARGET_TEXT, false),	
				new TargetDirective("anyintext:", TARGET_TEXT, true),
				new TargetDirective(":", TARGET_ALL, false),	
				new TargetDirective("any:", TARGET_ALL, true),
			};
			final TargetDirective def = directives[6];
			
			for (TargetDirective targetDirective : directives) {
				if (src.startsWith(targetDirective.expression)) {
					return targetDirective;
				}
			}
			
			return def;
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
