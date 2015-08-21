package com.civilizer.domain;

import java.io.Serializable;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import com.civilizer.utils.Pair;

@SuppressWarnings("serial")
public final class SearchParams implements Serializable {
	
	public static final int TARGET_DEFAULT       = 0;
	public static final int TARGET_TAG           = 1;
	public static final int TARGET_TITLE         = 2;
	public static final int TARGET_TEXT          = 3;
	public static final int TARGET_ID            = 4;
	
	private static final TargetDirective[] DIRECTIVES = {
	    new TargetDirective(":", TARGET_DEFAULT, false),    
	    new TargetDirective("any:", TARGET_DEFAULT, true),
        new TargetDirective("tag:", TARGET_TAG, false), 
        new TargetDirective("anytag:", TARGET_TAG, true),
        new TargetDirective("title:", TARGET_TITLE, false), 
        new TargetDirective("anytitle:", TARGET_TITLE, true),
        new TargetDirective("text:", TARGET_TEXT, false),   
        new TargetDirective("anytext:", TARGET_TEXT, true),
        new TargetDirective("id:", TARGET_ID, true),
    };
	
	private static final String TARGET_DIRECTIVE_PATTERN =
	        "(\\b(any|tag|tagh|anytag|title|anytitle|text|anytext|id)\\b)?:";
	
	private static final String CONTROL_OPERATORS = "cwberh-";
	
	public static final class Keyword implements Serializable {
		private final String word;
		private final boolean caseSensitive;
		private final boolean wholeWord;
		private final boolean beginningWith;
		private final boolean endingWith;
		private final boolean regex;
		private final boolean inverse;
		private final boolean id;
		private final boolean tagHeirarchy;
		
		public Keyword(String src, boolean isId) {
			String word = src.trim();
			boolean caseSensitive = false;
			boolean wholeWord = false;
			boolean beginningWith = false;
			boolean endingWith = false;
			boolean regex = false;
			boolean inverse = false;
			boolean id = isId;
			boolean tagHeirarchy = false;
			final Pattern p = Pattern.compile("(.*)/(["+CONTROL_OPERATORS+"]+)$");
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
				if (suffix.indexOf('h') != -1) {
				    // [RULE] .../- => hierarchy; applied to only tag keywords.
				    // The query will respect not only the tag but all its descendant tags.
				    tagHeirarchy = true;
				}
			}
			
			if (word.startsWith("\"") && word.endsWith("\"")) {
				// [RULE] "..." treats phrases containing spaces as a single unit
			    // Also any flag or directive inside the quotes will be treated as normal trivial characters
				if (word.length() > 1) {
					word = word.substring(1, word.length() - 1);
				}
			}
			
			if (beginningWith && endingWith)
				wholeWord = true;
			if (wholeWord)
			    beginningWith = endingWith = true;
			if (regex) {
			    // 'r' flag assumes case sensitivity regardless of the value of 'c' flag
			    caseSensitive = true;
			    // also 'r' flag ignores other pattern matching flags
			    wholeWord = beginningWith = endingWith = false;
			}
			
			this.word = word;
			this.caseSensitive = caseSensitive;
			this.wholeWord = wholeWord;
			this.beginningWith = beginningWith;
			this.endingWith = endingWith;
			this.regex = regex;
			this.inverse = inverse;
			this.id = id;
			this.tagHeirarchy = tagHeirarchy;
		}
		
		public static Pair<String, Character> escapeSqlWildcardCharacters(String word) {
			// Escape SQL wild cards used in pattern matching for 'where ... like ...' clause. ( '_' and '%')
			// When user provided words contain these characters, they are just trivial, non significant characters.
			// So escaping them is necessary before SQL treats them as wild cards
			
			final boolean hasUnderscore = (word.indexOf('_') != -1);
			final boolean hasPercent = (word.indexOf('%') != -1);
			char escapeChar = 0;
			
			if (hasUnderscore || hasPercent) {
			    // Find out a proper escape character satisfying one condition:
			    //   The character should not be included in the given word.
				for (int ascii=33; ascii<127; ++ascii) {
					if (ascii == '_' || ascii == '%' || ascii=='?' || ascii=='*'|| ascii=='\'' || ascii=='\"') {
					    // These characters are not suitable.
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

		public boolean matchesTagName(Tag tag) {
		    return matchesTagName(tag.getTagName());
		}
		
		public boolean matchesTagName(String tagName) {
		    final String w = caseSensitive ? word : word.toLowerCase();
		    final String name = caseSensitive ?  tagName : tagName.toLowerCase();
		    boolean match = false;
		    
		    if (regex) {
		        // 'r' flag assumes case is sensitive regardless of the value of 'c' flag
		        match = Pattern.matches(word, tagName);
		    }
		    else {
		        if (wholeWord) {
		            match = name.equals(w);
		        }
		        else if (beginningWith) {
		            match = name.startsWith(w);
		        }
		        else if (endingWith) {
		            match = name.endsWith(w);
		        }
		        else {
		            match = name.contains(w);
		        }
		    }
		    
		    return match;
		}
		
		private boolean checkValidity(String word) {
			boolean ok = true;
			if (word.isEmpty()) {
				ok = false;
			}
			else {
				if (isId()) {
					try {
						Long.parseLong(word);
					} catch (NumberFormatException e) {
						ok = false;
					}
				}
			}
			return ok;
		}
		
		public boolean isValid() {
			return checkValidity(word);
		}

		public boolean isTrivial() {
            return !regex && !wholeWord && !beginningWith && !endingWith && !id;
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
		
		public boolean isId() {
			return id;
		}

		public boolean tagHeirarchyRequired() {
		    return tagHeirarchy;
		}
		
		@Override
		public String toString() {
            return word;
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
		
		public Keywords() {
		    words = Collections.emptyList();
		    target = TARGET_DEFAULT;
		    any = false;
		}
		
		public Keywords(String src) {
			src = src.trim();
			List<Keyword> words = new ArrayList<Keyword>();
			final TargetDirective targetDirective = parseTarget(src);
			final int target = targetDirective.target;
			final boolean any = targetDirective.any;
			
			if (! src.isEmpty()) {
				final Pattern p =
				        Pattern.compile("(\"[^\"]+\"(/["+CONTROL_OPERATORS+"]+)?)|(\\S+)");
				boolean isId = false;
				boolean isTag = false;
				
				if (src.startsWith(targetDirective.expression)) {
					// The source string starts with an explicit directive such as 'any:', 'tag:', etc.
					// We skip the directive and pass the rest of the string.
					src = src.substring(targetDirective.expression.length());
					
					isId = (target == TARGET_ID);
					isTag = (target == TARGET_TAG);
				}
				
				final Matcher m = p.matcher(src);
				
				while (m.find()) {
				    String w = m.group();
				    if (isTag) {
				        // in case of tags, (as a special rule) commas can be attached with keywords;
				        // we trim the commas here
				        w = trimComma(w);
				        if (w == null) continue;
				    }
					final Keyword kw = new Keyword(w, isId);
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
		
		private static String trimComma(String input) {
		    String[] tmp = input.split(",");
		    for (String s : tmp) {
                s = s.trim();
                if (s.isEmpty()) continue;
                return s;
            }
		    return null;
		}
		
		private static TargetDirective parseTarget(String src) {
			final TargetDirective def = DIRECTIVES[0];
			
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
	
	public static final class TagCache {
	    private static final class Key {
	        final long       id;
	        final boolean    inverse;
	        
	        Key() {
	            this.id = Tag.TRASH_TAG_ID; this.inverse = false;
	        }

	        Key(long id, boolean inverse) {
	            this.id = id; this.inverse = inverse;
	        }
	        
	        public String toString() {
	            return new Long(id).toString() + (inverse ? " -" : "");
	        }
	    }
	    
	    private List<List<Key>> keys = new ArrayList<List<Key>>();
	    
	    public TagCache(Collection<Tag> tags, SearchParams sp) {
            if (tags == null || tags.isEmpty())
                return;
	        List<Keyword> kwlst = Collections.emptyList();
	        
	        Keywords kws = sp.getKeywords(TARGET_TAG, false);
	        if (kws != null) kwlst = kws.getWords();
	        for (Keyword w : kwlst) {
	            final List<Key> tmp = new ArrayList<>();
	            for (Tag t : tags) {
	                if (w.matchesTagName(t)) {
                        if (w.tagHeirarchyRequired()){
                            List<Key> hks = getHierarchyKeys(t, w.isInverse());
                            tmp.addAll(hks);
                        }
                        else
                            tmp.add(new Key(t.getId(), w.isInverse()));
                    }
	            }
	            if (!tmp.isEmpty())
	                keys.add(tmp);
            }
            if (!kwlst.isEmpty() && keys.isEmpty()) {
                // [NOTE] If the search phrase contains names of nonexistent tags,
                // we need to insert a dummy tag id that won't match any fragment.
                // unless doing this, search with nonexistent tags will match every fragment.
                final List<Key> tmp = new ArrayList<>();
                tmp.add(new Key());
                keys.add(tmp);
            }
	        
	        kws = sp.getKeywords(TARGET_TAG, true);
	        if (kws != null) kwlst = kws.getWords();
	        final List<Key> tmp = new ArrayList<>();
	        for (Keyword w : kwlst) {
                for (Tag t : tags) {
                    if (w.matchesTagName(t)) {
                        if (w.tagHeirarchyRequired()) {
                            List<Key> hks = getHierarchyKeys(t, w.isInverse());
                            tmp.addAll(hks);
                        }
                        else
                            tmp.add(new Key(t.getId(), w.isInverse()));
                    }
                }
            }
            if (!kwlst.isEmpty() && tmp.isEmpty())
                tmp.add(new Key());
            if (!tmp.isEmpty())
                keys.add(tmp);
	    }
	    
	    private static List<Key> getHierarchyKeys(Tag tag, boolean inverse) {
	        Set<Long> idSet = tag.getIdsOfDescendants(true);
	        if (idSet.isEmpty())
	            return Collections.emptyList();
	        final List<Key> output = new ArrayList<Key>();
	        for (Long id : idSet) {
	            output.add(new Key(id, inverse));
	        }
	        return output;
	    }
	    
	    public boolean matches(Fragment frg) {
	        if (frg.containsTagId(Tag.TRASH_TAG_ID))
	            // Exclude trashed tags regardless of the search phrase.
	            return false;
	        
            for (List<Key> klst : keys) {
                assert !klst.isEmpty();
                boolean outerMatch = false;
                for (Key k : klst) {
                    boolean innerMatch = frg.containsTagId(k.id);
//                    if (!k.inverse && innerMatch || k.inverse && !innerMatch) {
                    if (k.inverse ^ innerMatch) {
                        outerMatch = true;
                        break;
                    }
                }
                if (!outerMatch)
                    return false;
            }
            
	        return true;
	    } 
        
        public boolean valid() {
            return !keys.isEmpty();
        }
	}
	
	private final List<Keywords> keywords;
	private final String searchPhrase;

	public SearchParams(String src) {
		src = src.trim();
		
		// Our objective is to populate the following object
		List<Keywords> keywords = new ArrayList<Keywords>();

		// Match directives
		final List<Pair<Integer, Integer>> ranges = new ArrayList<Pair<Integer, Integer>>();
	    Pattern p = Pattern.compile(TARGET_DIRECTIVE_PATTERN);
	    Matcher m = p.matcher(src);
	    
	    while (m.find()) {
	        ranges.add(new Pair<Integer, Integer>(m.start(), m.end()));
	    }

	    // We should ignore any directive existing inside double quotes (as-is block)
	    p = Pattern.compile("(\"([^\"])+\")");
	    m = p.matcher(src);
	    while (m.find()) {
	        Iterator<Pair<Integer, Integer>> itr = ranges.iterator();
	        while (itr.hasNext()) {
	            Pair<Integer, Integer> range = itr.next();
	            if (m.start() <= range.getFirst() && range.getSecond() <= m.end()) {
	                // If this directive is inside double quotes (as-is block), it is not intended as a directive.
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
	    this.searchPhrase = src;
	}

	public List<Keywords> getKeywords() {
		return keywords == null ?
				Collections.<Keywords>emptyList() : keywords;
	}

    public String getSearchPhrase() {
        return searchPhrase;
    }
    
	public Keywords getKeywords(int target, boolean any) {
	    // [RULE] we may have multiple instances of same target type.
	    // e.g. tag:...  title:...  tag:... 
	    //     => two instances of Keywords class for TARGET_TAG
	    // we simply accept the 1st instance only and ignore all of the rest.
	    // there is no benefit the users write their search phrase in this way.
	    // so we have no need to care.
		for (Keywords words : keywords) {
			if (words.getTarget() == target && words.isAny() == any) {
				return words;
			}
		}
		return null;
	}

}
