package com.knowledgex.domain;

import java.io.Serializable;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

@SuppressWarnings("serial")
public final class SearchParams implements Serializable {
	
	public static final int TARGET_ALL     = 0;
	public static final int TARGET_TAG     = 1;
	public static final int TARGET_TITLE   = 2;
	public static final int TARGET_TEXT    = 3;
	
	private static final TargetDirective[] DIRECTIVES = {
        new TargetDirective("tag:", TARGET_TAG, false), 
        new TargetDirective("anytag:", TARGET_TAG, true),
        new TargetDirective("title:", TARGET_TITLE, false), 
        new TargetDirective("anytitle:", TARGET_TITLE, true),
        new TargetDirective("text:", TARGET_TEXT, false),   
        new TargetDirective("anytext:", TARGET_TEXT, true),
        new TargetDirective(":", TARGET_ALL, false),    
        new TargetDirective("any:", TARGET_ALL, true),
    };
	
	private static final String TARGET_DIRECTIVE_PATTERN =
	        "(\\b(any|tag|anytag|title|anytitle|text|anytext)\\b)?:";
	
	public static final class Keyword implements Serializable {
		private final String word;
		private final boolean caseSensitive;
		private final boolean wholeWord;
		private final boolean beginningWith;
		private final boolean endingWith;
		private final boolean regex;
		private final boolean inverse;
		
		public Keyword(String src) {
			String word = src.trim();
			boolean caseSensitive = false;
			boolean wholeWord = false;
			boolean beginningWith = false;
			boolean endingWith = false;
			boolean regex = false;
			boolean inverse = false;
			
			final Pattern p = Pattern.compile("(.*)/([cwber-]+)$");
			final Matcher m = p.matcher(src);
			
			if (m.find()) {
				word = m.group(1);
				final String suffix = m.group(2);
				if (suffix.indexOf('c') != -1) {
					// [RULE] .../c => case sensitive
					caseSensitive = true;
				}
				if (suffix.indexOf('w') != -1) {
					// [RULE] .../w => whole word
					wholeWord = true;
				}
				if (suffix.indexOf('b') != -1) {
					// [RULE] .../b => beginning with
					beginningWith = true;
				}
				if (suffix.indexOf('e') != -1) {
					// [RULE] .../e => ending with
					endingWith = true;
				}
				if (suffix.indexOf('r') != -1) {
					// [RULE] .../r => regular expression
					regex = true;
				}
				if (suffix.indexOf('-') != -1) {
					// [RULE] .../- => inverse; the query returns data not matching the pattern.
					inverse = true;
				}
			}
			
			if (word.startsWith("\"") && word.endsWith("\"")) {
				// [RULE] "..." treats phrases containing spaces as a single unit
			    // Also any flag or directive inside the quotes will be treated as normal trivial characters
				if (word.length() > 1) {
					word = word.substring(1, word.length() - 1);
				}
			}
			
			if (beginningWith && endingWith) {
				wholeWord = true;
			}
			
			this.word = word;
			this.caseSensitive = caseSensitive;
			this.wholeWord = wholeWord;
			this.beginningWith = beginningWith;
			this.endingWith = endingWith;
			this.regex = regex;
			this.inverse = inverse;
		}
		
		public static Pair<String, Character> escapeSqlWildcardCharacters(String word) {
			// Escape SQL wild cards used in pattern matching for 'where ... like ...' clause. ( '_' and '%')
			// When user provided words contain these characters, they are just trivial, non significant characters.
			// So escaping them is necessary before SQL treats them as wild cards
			
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
		
		private static boolean checkValidity(String word) {
			return ! word.isEmpty();
		}
		
		public boolean isValid() {
			return checkValidity(word);
		}

		public boolean isTrivial() {
            return !regex && !wholeWord && !beginningWith && !endingWith;
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

		public boolean isBeginningWith() {
			return beginningWith;
		}

		public boolean isEndingWith() {
			return endingWith;
		}
		
		public boolean isRegex() {
			return regex;
		}
		
		public boolean isInverse() {
			return inverse;
		}
	}
	
	private static final class TargetDirective implements Serializable {
		public final String expression;
		public final int target;
		public final boolean any;
		
		public TargetDirective(String expr, int target, boolean any) {
			this.expression = expr.intern();
			this.target = target;
			this.any = any;
		}
	}
	
	public static final class Keywords implements Serializable {
		private final List<Keyword> words;
		private final int target;
		private final boolean any;
		
		public Keywords(String src) {
			src = src.trim();
			List<Keyword> words = new ArrayList<Keyword>();
			final TargetDirective targetDirective = parseTarget(src);
			int target = targetDirective.target;
			boolean any = targetDirective.any;
			
			if (! src.isEmpty()) {
				final Pattern p = Pattern.compile("(\"[^\"]+\")|(\\S+)");
				
				if (src.startsWith(targetDirective.expression)) {
					// The source string starts with an explicit directive such as 'any:', 'tag:', etc.
					// We skip the directive and pass the rest of the string.
					src = src.substring(targetDirective.expression.length());
				}
				
				final Matcher m = p.matcher(src);
				
				while (m.find()) {
					final Keyword kw = new Keyword(m.group());
					if (kw.isValid()) {
						words.add(kw);
					}
				}
			}
			
			if (words.isEmpty()) {
				words = Collections.emptyList();
			}
			
			this.words = words;
			this.target = target;
			this.any = any;
		}
		
		private static TargetDirective parseTarget(String src) {
			final TargetDirective def = DIRECTIVES[6];
			
			for (TargetDirective targetDirective : DIRECTIVES) {
				if (src.startsWith(targetDirective.expression)) {
					return targetDirective;
				}
			}
			
			return def; // No directive specified, which means ':' is specified implicitly
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

	public SearchParams(String src) {
		src = src.trim();
	    final List<Pair<Integer, Integer>> ranges = new ArrayList<Pair<Integer, Integer>>();
	    List<Keywords> keywords = new ArrayList<Keywords>();
	    Pattern p = Pattern.compile(TARGET_DIRECTIVE_PATTERN);
	    Matcher m = p.matcher(src);
	    
	    while (m.find()) {
	        ranges.add(new Pair<Integer, Integer>(m.start(), m.end()));
	    }

	    // We should ignore any directive existing inside '' quotes (as-is block)
	    p = Pattern.compile("(\"([^\"])+\")");
	    m = p.matcher(src);
	    while (m.find()) {
	        Iterator<Pair<Integer, Integer>> itr = ranges.iterator();
	        while (itr.hasNext()) {
	            Pair<Integer, Integer> range = itr.next();
	            if (m.start() <= range.getFirst() && range.getSecond() <= m.end()) {
	                // If this directive is inside '' block (as-is block), it is not intended as a directive.
	                itr.remove();
	            }
	        }
	    }
	    
	    if (ranges.isEmpty() || ranges.get(0).getFirst() > 0) {
	        // We have no directive found at the beginning of the input string.
	    	// It is identical to the ':' directive mode.
	        ranges.add(0, new Pair<Integer, Integer>(0, 0));
	    }
	    
	    ranges.add(new Pair<Integer, Integer>(src.length(), src.length()));
	    
	    for (int i=1; i<ranges.size(); ++i) {
	        final Pair<Integer, Integer> r0 = ranges.get(i - 1);
	        final Pair<Integer, Integer> r1 = ranges.get(i);
	        final String s = src.substring(r0.getFirst(), r1.getFirst());
	        final Keywords kws = new Keywords(s);
	        if (! kws.getWords().isEmpty()) {
	        	keywords.add(kws);
	        }
	    }
	    
	    if (keywords.isEmpty()) {
	    	keywords = Collections.emptyList();
	    }
	    
	    this.keywords = keywords;
	}

	public List<Keywords> getKeywords() {
		return keywords;
	}
	
	public boolean hasTarget(int target) {
		boolean hasIt = false;
		for (Keywords words : keywords) {
			if (words.getTarget() == target) {
				hasIt = true;
				break;
			}
		}
		return hasIt;
	}

}
