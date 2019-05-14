package studio.lineage2.cms.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import studio.lineage2.cms.model.News;
import studio.lineage2.cms.repository.NewsRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 Created by iRock
 28.10.2015
 */
@Service
public class NewsService
{
	@Autowired
	@Qualifier("newsRepository")
	private NewsRepository newsRepository;

	public void save(News news)
	{
		newsRepository.save(news);
	}

	public void delete(long id)
	{
		newsRepository.delete(id);
	}

	public List<News> findAll()
	{
		return newsRepository.findAll();
	}

	@Cacheable("news")
	public List<News> findAll(String lang)
	{
		return newsRepository.findAll().stream().filter(news -> news.getLang().equals(lang)).collect(Collectors.toList());
	}

	public News findOne(long id)
	{
		return newsRepository.findOne(id);
	}
}