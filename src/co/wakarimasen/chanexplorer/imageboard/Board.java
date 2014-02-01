package co.wakarimasen.chanexplorer.imageboard;

import java.util.ArrayList;
import java.util.List;

public class Board {
	
	private boolean worksafe;
	private String id;
	private String name;
	private Category category;
	
	public static final List<Category> categories = new ArrayList<Category>();
	public static final List<Board> boards = new ArrayList<Board>();
	
	static {
		Category favorites = new Category("Favorites");
		Category jp = new Category("Japanese Culture");
		Category interests = new Category("Interests");
		Category creative = new Category("Creative");
		Category adult = new Category("Adult");
		Category other = new Category("Other");
		Category misc = new Category("Misc.");
		categories.add(favorites);
		categories.add(jp); categories.add(interests);
		categories.add(creative); categories.add(adult);
		categories.add(other); categories.add(misc);
		
		boards.add(new Board("a", "Anime & Manga", jp, true));
		boards.add(new Board("c", "Anime/Cute", jp, true));
		boards.add(new Board("w", "Anime/Wallpapers", jp, true));
		boards.add(new Board("m", "Mecha", jp, true));
		boards.add(new Board("cgl", "Cosplay & EGL", jp, true));
		boards.add(new Board("cm", "Cute/Male", jp, true));
		boards.add(new Board("n", "Transportation", jp, true));
		boards.add(new Board("jp", "Otaku Culture", jp, true));
		boards.add(new Board("vp", "Pokémon", jp, true));
		boards.add(new Board("v", "Video Games", interests, true));
		boards.add(new Board("vg", "Video Game Generals", interests, true));
		boards.add(new Board("vr", "Retro Games", interests, true));
		boards.add(new Board("co", "Comics & Cartoons", interests, true));
		boards.add(new Board("g", "Technology", interests, true));
		boards.add(new Board("tv", "Television & Film", interests, true));
		boards.add(new Board("k", "Weapons", interests, true));
		boards.add(new Board("o", "Auto", interests, true));
		boards.add(new Board("an", "Animals & Nature", interests, true));
		boards.add(new Board("tg", "Traditional Games", interests, true));
		boards.add(new Board("sp", "Sports", interests, true));
		boards.add(new Board("asp", "Alternative Sports", interests, true));
		boards.add(new Board("sci", "Science & Math", interests, true));
		boards.add(new Board("int", "International", interests, true));
		boards.add(new Board("out", "Outdoors", interests, true));
		boards.add(new Board("i", "Oekaki", creative, true));
		boards.add(new Board("po", "Papercraft & Origami", creative, true));
		boards.add(new Board("p", "Photography", creative, true));
		boards.add(new Board("ck", "Food & Cooking", creative, true));
		boards.add(new Board("ic", "Artwork/Critique", creative, true));
		boards.add(new Board("wg", "Wallpapers/General", creative, true));
		boards.add(new Board("mu", "Music", creative, true));
		boards.add(new Board("fa", "Fashion", creative, true));
		boards.add(new Board("toy", "Toys", creative, true));
		boards.add(new Board("3", "3DCG", creative, true));
		boards.add(new Board("gd", "Graphic Design", creative, true));
		boards.add(new Board("diy", "Do-It-Yourself", creative, true));
		boards.add(new Board("wsg", "Worksafe GIF", creative, true));
		boards.add(new Board("s", "Sexy Beautiful Women", adult, false));
		boards.add(new Board("hc", "Hardcore", adult, false));
		boards.add(new Board("hm", "Handsome Men", adult, false));
		boards.add(new Board("h", "Hentai", adult, false));
		boards.add(new Board("e", "Ecchi", adult, false));
		boards.add(new Board("u", "Yuri", adult, false));
		boards.add(new Board("d", "Hentai/Alternative", adult, false));
		boards.add(new Board("y", "Yaoi", adult, false));
		boards.add(new Board("t", "Torrents", adult, false));
		boards.add(new Board("hr", "High Resolution", adult, false));
		boards.add(new Board("gif", "Animated GIF", adult, false));
		boards.add(new Board("q", "4chan Discussion", other, true));
		boards.add(new Board("trv", "Travel", other, true));
		boards.add(new Board("fit", "Health & Fitness", other, true));
		boards.add(new Board("x", "Paranormal", other, true));
		boards.add(new Board("lit", "Literature", other, true));
		boards.add(new Board("lgbt", "LGBT", other, true));
		boards.add(new Board("adv", "Advice", other, true));
		boards.add(new Board("mlp", "Pony", other, true));
		boards.add(new Board("b", "Random", misc, false));
		boards.add(new Board("r", "Request", misc, false));
		boards.add(new Board("r9k", "ROBOT9001", misc, false));
		boards.add(new Board("pol", "Politically Incorrect", misc, false));
		boards.add(new Board("soc", "Social", misc, false));
		boards.add(new Board("s4s", "Shit 4chan Says", misc, false));
	}
	
	public boolean isWorksafe() {
		return worksafe;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Category getCategory() {
		return category;
	}

	
	public Board(String id, String name, Category category, boolean worksafe) {
		this.id = id;
		this.name = name;
		this.worksafe = worksafe;
		this.category = category;
	}
	
	public Board asFavorite() {
		return new Board(id, name, Board.categories.get(0), worksafe);
	}
	
	@Override
	public int hashCode() {
		return id.hashCode();
	}
	
	@Override
	public boolean equals(Object other) {
		if (other instanceof Board)
			return id.equals(((Board)other).getId());
		return false;
	}
	
	@Override
	public String toString() {
		return getId();
	}
	
	public static Board getBoardById(String id) {
		for (int i=0; i<boards.size(); i++) {
			if (boards.get(i).getId().equals(id))
				return boards.get(i);
		}
		return null;
	}
	
	public static class Category {
		private String name;
		
		public Category(String name) {
			this.name = name;
		}
		
		public String getName() {
			return name;
		}
	}
}
