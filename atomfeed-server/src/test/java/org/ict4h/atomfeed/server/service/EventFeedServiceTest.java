package org.ict4h.atomfeed.server.service;

import com.sun.syndication.feed.atom.*;
import junit.framework.Assert;
import org.ict4h.atomfeed.server.domain.EventRecord;
import org.ict4h.atomfeed.server.domain.EventRecordsOffsetMarker;
import org.ict4h.atomfeed.server.domain.chunking.ChunkingHistoryEntry;
import org.ict4h.atomfeed.server.repository.AllEventRecordsStub;
import org.ict4h.atomfeed.server.repository.ChunkingEntries;
import org.ict4h.atomfeed.server.repository.AllEventRecordsOffsetMarkers;
import org.ict4h.atomfeed.server.repository.InMemoryEventRecordCreator;
import org.ict4h.atomfeed.server.service.feedgenerator.FeedGenerator;
import org.ict4h.atomfeed.server.service.feedgenerator.NumberFeedGenerator;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class EventFeedServiceTest {
    private EventFeedService eventFeedService;
    private AllEventRecordsStub allEventRecords;
    private InMemoryEventRecordCreator recordCreator;
    private String category;

    private ChunkingEntries allChunkingEntries = new ChunkingEntries() {
        @Override
        public List<ChunkingHistoryEntry> all() {
            List<ChunkingHistoryEntry> entries = new ArrayList<>();
            entries.add(new ChunkingHistoryEntry(1, 5L, 1L));
            return entries;
        }
    };

    @Before
    public void setupEventRecords() throws URISyntaxException {
        allEventRecords = new AllEventRecordsStub();
        AllEventRecordsOffsetMarkers allEventRecordsOffsetMarkers = new AllEventRecordsOffsetMarkers() {
            @Override
            public void addOrUpdate(String category, Integer offsetId, Integer countTillOffSetId) {
            }

            @Override
            public List<EventRecordsOffsetMarker> getAll() {
                return new ArrayList<>();
            }
        };
        recordCreator = new InMemoryEventRecordCreator(allEventRecords);
        category = "category";
        recordCreator.create(7, category);
        
        FeedGenerator feedGenerator = new NumberFeedGenerator(allEventRecords, allEventRecordsOffsetMarkers, allChunkingEntries);
        eventFeedService = new EventFeedServiceImpl(feedGenerator);
    }

    @Test
    public void shouldGetRecentFeed() throws URISyntaxException {
        String recentUrl = "http://hostname/feedgenerator/recent";
        Feed feed = eventFeedService.getRecentFeed(new URI(recentUrl), null);
        HashMap<String, Link> links = getAllFeedLinks(feed);
        Assert.assertNull(links.get("next-archive"));
        Assert.assertNotNull(links.get("prev-archive"));
        assertEquals(recentUrl, links.get("self").getHref());
    }

    @Test
    public void shouldGetAnAuthorForTheFeed() throws URISyntaxException {
        String recentUrl = "http://hostname/feedgenerator/recent";
        Feed feed = eventFeedService.getRecentFeed(new URI(recentUrl), null);
        assertEquals("OpenMRS", ((Person) feed.getAuthors().get(0)).getName());
    }

    @Test
    public void shouldSetUpdatedDateToTodayWhenNoRecordsCanBeFound() throws URISyntaxException {
        String recentUrl = "http://hostname/feedgenerator/recent";
        allEventRecords.clear();
        Calendar date = new GregorianCalendar();
        date.set(Calendar.HOUR_OF_DAY, 0);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);

        Feed feed = eventFeedService.getRecentFeed(new URI(recentUrl), null);
        assertEquals(date.getTime(), feed.getUpdated());
    }

    private HashMap<String, Link> getAllFeedLinks(Feed feed) {
        HashMap<String, Link> hashMap = new HashMap<>();
        List<Link> alternateLinks = feed.getAlternateLinks();
        for (Link link : alternateLinks) {
            hashMap.put(link.getRel(), link);
        }
        return hashMap;
    }

    @Test
    public void shouldGetEventFeed() throws URISyntaxException {
        String feedUrl = "http://hostname/feedgenerator/1";
        Feed feed = eventFeedService.getEventFeed(new URI(feedUrl), "category", 1);
        HashMap<String, Link> links = getAllFeedLinks(feed);
        Assert.assertNotNull(links.get("next-archive"));
        Assert.assertNull(links.get("prev-archive"));
        assertEquals(feedUrl, links.get("self").getHref());
    }

    @Test
    public void shouldGenerateTheSameUUIDForFeedsThatAreNotRecent() throws URISyntaxException {
        String feedUrl = "http://hostname/feedgenerator/1";
        Feed feed = eventFeedService.getEventFeed(new URI(feedUrl), "category", 2);
        Feed theSameFeed = eventFeedService.getEventFeed(new URI(feedUrl), "category", 2);
        assertEquals(feed.getId(),theSameFeed.getId());
    }

    @Test
    public void shouldGetContentsFromFeedWrappedInCDATA() throws URISyntaxException {
        String recentUrl = "http://hostname/feedgenerator/1";
        Feed feed = eventFeedService.getRecentFeed(new URI(recentUrl), null);
        Entry entry = (Entry) feed.getEntries().get(0);
        String contents = ((Content)(entry.getContents().get(0))).getValue();
        Assert.assertTrue(contents.startsWith("<![CDATA["));
        Assert.assertTrue(contents.endsWith("]]>"));
    }

    @Test
    public void shouldNotWrapContentsInCDATAWhenContentsAreNotPresent() throws URISyntaxException {
        String recentUrl = "http://hostname/feedgenerator/1";
        allEventRecords.clear();
        recordCreator.create(new EventRecord("","", "",null,new Date(), ""));
        Feed feed = eventFeedService.getRecentFeed(new URI(recentUrl), null);
        Entry entry = (Entry) feed.getEntries().get(0);
        Assert.assertNull(((Content)entry.getContents().get(0)).getValue());
    }

    @Test
    public void shouldReadCategoryFromFeed() throws URISyntaxException {
        String recentUrl = "http://hostname/feedgenerator/1";
        Feed feed = eventFeedService.getRecentFeed(new URI(recentUrl), category);
        Entry entry = (Entry) feed.getEntries().get(0);
        assertEquals("category", ((Category)entry.getCategories().get(0)).getTerm());
    }

    @Test
    public void shouldNotPopulateCategoryWhenCategoryIsNotPresent() throws URISyntaxException {
        allEventRecords.clear();
        recordCreator.create(1,null);
        String recentUrl = "http://hostname/feedgenerator/recent";
        Feed feed = eventFeedService.getRecentFeed(new URI(recentUrl), null);
        Entry entry = (Entry) feed.getEntries().get(0);
        assertTrue(entry.getCategories().isEmpty());
    }
}
