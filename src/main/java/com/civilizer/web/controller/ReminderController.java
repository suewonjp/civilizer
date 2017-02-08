package com.civilizer.web.controller;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import com.civilizer.web.view.*;
import com.civilizer.config.AppOptions;
import com.civilizer.config.Configurator;
import com.civilizer.dao.FragmentDao;
import com.civilizer.domain.Fragment;

@Controller
@Component("reminderController")
public final class ReminderController {
    
    private static final String POLL_CLIENT_ID = "fragment-group-form:reminder-poll";
    
    @Autowired
    private FragmentDao fragmentDao;

    private Random getRandom() {
        final Calendar cal = Calendar.getInstance();
        final long seed = cal.getTimeInMillis();
        return new Random(seed);
    }

    private Fragment selectFragmentReminder(Random rand, long fragmentCount) {
        final long fid = rand.nextLong() % fragmentCount;
        return fragmentDao.findById(fid); // [NOTE] Sometimes returns null
    }
    
    private String buildReminderContent(Random rand) {
        String content = "";
        final long fc = fragmentDao.countAll(false);
        final List<Long> frgs = new ArrayList<>();
        frgs.add(-1L);
        // Randomly choose fragments and build links to them
        for (int i=0; i<3; ++i) {
            final Fragment frg = selectFragmentReminder(rand, fc);
            final Long fid = (frg == null) ? -1L : frg.getId();
            if (frgs.contains(fid))
                continue; // Skip a null or duplicate fragment
            frgs.add(fid);
            content += "<a class='-cvz-frgm' href='fragment/" + fid + "'>" +
                "<span class='ui-state-default ui-corner-all'>" +
                frg.getTitle() +
                "</span><a/>";
        }
        if (!content.isEmpty())
            content = "<br/>" + content + "<br/>";
        return content;
    }

    private String buildPromotionContent(Random rand) {
        return "<span class='fa fa-heart fa-rgap'>" + ViewUtil.getResourceBundleString("support_cvz") + "</span>" +
                "<a target='_blank' href='https://twitter.com/intent/follow?screen_name=civilizer_pkm'>" +
                "<span class='fa fa-rgap fa-twitter twitter-button ui-state-default ui-corner-all'>" +
                "Follow @civilizer_pkm" +
                "</span></a>";
    }

    private String buildSettingContent() {
        return "<a href='#' class='fa fa-gear fa-rgap reminder-setting-btn' onclick='onClickReminderSetting(this)'>Setting</a>";
    }

    public void onReminderRequest() {
        if (Configurator.equals(AppOptions.REMINDER_INTERVAL, "0", false)) {
            return;
        }
        final Random rand = getRandom();
        String detail = buildReminderContent(rand);
        if (detail.isEmpty()) {
            return;
        }
        String title = ViewUtil.getResourceBundleString("remember_fragments");
        detail += buildPromotionContent(rand);
        detail += buildSettingContent();
        ViewUtil.addMessage(POLL_CLIENT_ID, title, detail, null);
    }

}
