package com.knowledgex.domain;

import java.util.*;

public final class SearchParams {
	
	public static final class Keywords {
		private final List<String> words;
		private final boolean caseSensitive;
		private final boolean any;
		
		Keywords(List<String> words, boolean caseSensitive, boolean any) {
			this.words = words;
			this.caseSensitive = caseSensitive;
			this.any = any;
		}

		public List<String> getWords() {
			return words;
		}

		public boolean isCaseSensitive() {
			return caseSensitive;
		}

		public boolean isAny() {
			return any;
		}
	}
	
	private final List<String> tags;
	private final Keywords keywordsForAnywhere;
	private final Keywords keywordsForTitle;
	private final Keywords keywordsForText;
	private final Keywords keywordsForUrl;
	
	SearchParams(
			List<String> tags
			, Keywords keywordsForAnywhere
			, Keywords keywordsForTitle
			, Keywords keywordsForText
			, Keywords keywordsForUrl
	) {
		this.tags = tags;
		this.keywordsForAnywhere = keywordsForAnywhere;
		this.keywordsForTitle = keywordsForTitle;
		this.keywordsForText = keywordsForText;
		this.keywordsForUrl = keywordsForUrl;
	}

	public List<String> getTags() {
		return tags;
	}

	public Keywords getKeywordsForAnywhere() {
		return keywordsForAnywhere;
	}

	public Keywords getKeywordsForTitle() {
		return keywordsForTitle;
	}

	public Keywords getKeywordsForText() {
		return keywordsForText;
	}

	public Keywords getKeywordsForUrl() {
		return keywordsForUrl;
	}

}
