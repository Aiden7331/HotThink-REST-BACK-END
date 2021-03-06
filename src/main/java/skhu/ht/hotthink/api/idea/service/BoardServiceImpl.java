package skhu.ht.hotthink.api.idea.service;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import skhu.ht.hotthink.api.domain.*;
import skhu.ht.hotthink.api.domain.enums.BoardReferenceType;
import skhu.ht.hotthink.api.domain.enums.BoardType;
import skhu.ht.hotthink.api.domain.enums.UseAt;
import skhu.ht.hotthink.api.idea.exception.*;
import skhu.ht.hotthink.api.idea.model.LikeDTO;
import skhu.ht.hotthink.api.idea.model.LikeOutDTO;
import skhu.ht.hotthink.api.idea.model.PutDTO;
import skhu.ht.hotthink.api.idea.model.boardin.BoardInDTO;
import skhu.ht.hotthink.api.idea.model.boardin.SubRealInDTO;
import skhu.ht.hotthink.api.idea.model.boardlist.BoardListDTO;
import skhu.ht.hotthink.api.idea.model.boardout.BoardOutDTO;
import skhu.ht.hotthink.api.idea.model.page.Pagination;
import skhu.ht.hotthink.api.idea.model.reply.ReplyInDTO;
import skhu.ht.hotthink.api.idea.model.reply.ReplyOutDTO;
import skhu.ht.hotthink.api.idea.model.reply.ReplyPutDTO;
import skhu.ht.hotthink.api.idea.repository.*;
import skhu.ht.hotthink.api.payment.exception.MoneyException;
import skhu.ht.hotthink.api.user.exception.UserConflictException;
import skhu.ht.hotthink.api.user.exception.UserNotFoundException;
import skhu.ht.hotthink.api.user.model.UserBase;
import skhu.ht.hotthink.api.user.repository.UserRepository;
import skhu.ht.hotthink.api.idea.repository.RealRepository;
import skhu.ht.hotthink.error.ErrorCode;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BoardServiceImpl {
    private static final Logger log = LoggerFactory.getLogger(BoardServiceImpl.class);
    @Autowired
    BoardRepository boardRepository;
    @Autowired
    ReplyRepository replyRepository;
    @Autowired
    LikeRepository likeRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    RealRepository realRepository;
    @Autowired
    HistoryRepository historyRepository;
    @Autowired
    AttachRepository attachRepository;

    @Autowired
    ModelMapper modelMapper = new ModelMapper();

    /*
        작성자: 홍민석
        작성일: 19-10-04
        내용: Free, Hot, RealThink게시판 리스트를 반환합니다.
    */
    @Transactional
    public <Tlist extends BoardListDTO, Tpage extends Pagination> List<Tlist> getBoardList(Tpage pagination, Class<? extends Tlist> classLiteral) {
        Category category = categoryRepository.findCategoryByCategory(pagination.getCategory());
        List<Tlist> tlist = boardRepository.findAll(pagination,category)
                .stream()
                .map(e -> convertTo(e, classLiteral))
                .collect(Collectors.toList());
        if(tlist==null) throw new IdeaNotFoundException();
        return tlist;
    }

    /*
        작성자: 홍민석
        작성일: 19-10-04
        내용: 인자값으로 주어지는 게시물번호와 카테고리를 확인한 후
        해당하는 RealThink게시물을 반환합니다.
    */
    @Transactional
    public <Tout extends BoardOutDTO> Tout getOne(Long seq,BoardType boardType, Class<? extends Tout> classLiteral){
        Board board = boardRepository.findBoardByBdSeq(seq);
        if(board == null) throw new IdeaNotFoundException();
        if(!board.getBoardType().equals(boardType))
            throw new IdeaInvalidException(boardType.name().concat("게시물이 아닙니다."));
        board.setHits(board.getHits() + 1);
        boardRepository.save(board);
        return modelMapper.map(board, classLiteral);
    }
    /*
        작성자: 홍민석
        작성일: 19-10-22
        내용: 게시물을 생성합니다.
        작성일: 19-12-02
        내용: REAL THINK 생성시 FreePass권 1회 소모.
        실패하면 False 반환.
     */
    @Transactional
    public <Tin extends BoardInDTO> boolean setOne(Tin inDto, String category, BoardType boardType) {
        Long seq;
        //REAL THINK FreePass권으로 구매 후 사용시 수행
        if(boardType.equals(BoardType.REAL) && !useFreePass()) return false;
        Category categ = categoryRepository.findCategoryByCategory(category);
        if ((seq = boardRepository.findBoardSeq(category, boardType.name())) == -1) throw new IdeaInvalidException();
        User user = userRepository.findUserByEmail(findEmailBySpringSecurity());
        if(user==null) throw new UserNotFoundException();
        Board board = Board.FreeHotBuilder()
                .category(categ)
                .user(user)
                .boardType(boardType)
                .seq(seq)
                .title(inDto.getTitle())
                .contents(inDto.getContents())
                .createAt(new Date())
                .build();
        board = boardRepository.save(board);
        if(board == null) return false;
        if(!inDto.getAttaches().isEmpty()) {
            final Long boardSeq = board.getBdSeq();//아래 lambda 에서 사용.
            List<Attach> attaches = inDto.getAttaches()
                    .stream()
                    .map(e-> {
                        Attach temp = modelMapper.map(e, Attach.class);
                        temp.setBoardReferenceType(BoardReferenceType.BOARD);
                        temp.setBoardSeq(boardSeq);
                        return temp;
                    })
                    .collect(Collectors.toList());
            attachRepository.saveList(attaches);
        }
        return true;
    }
    /*
            작성자: 홍민석
            작성일: 19-10-26
            내용: FreeThink, RealThink, HotThink 게시물을 수정합니다.
            작성일: 19-11-26
            내용: HotThink를 RealThink로 전환하는 기능 작성
    */
    @Transactional
    public boolean putOne(PutDTO putDto) {
        if(putDto.getBoardType()==null) {
            throw new IdeaInvalidException("Board Type Not Found");
        }
        Board board = boardRepository.findBoardByBdSeq(putDto.getBdSeq());
        if(!isWriter(board.getUser().getEmail())) throw new UserUnauthorizedException("Access Deny");
        BoardInDTO original = modelMapper.map(board,BoardInDTO.class);
        BoardInDTO recent = modelMapper.map(putDto,BoardInDTO.class);
        if(!original.equals(recent)) {
            History history = History.builder()
                    .board(board)
                    .contents(original.getContents())
                    .title(original.getTitle())
                    .build();
            historyRepository.save(history);
            board.setTitleAndContents(recent.getTitle(),recent.getContents());
            boardRepository.save(board);
        }
        if(putDto.getBoardType()==BoardType.REAL) {
            SubRealInDTO rOriginal = null;
            if(!board.getReals().isEmpty()) {
                rOriginal = modelMapper.map(board.getReals().get(0), SubRealInDTO.class);
            }else {
                rOriginal = new SubRealInDTO();
                board.setBoardType(BoardType.REAL);
                boardRepository.save(board);
            }
            SubRealInDTO rRecent = putDto.getReal();
            if(!rOriginal.equals(rRecent)){
                Real real = modelMapper.map(rRecent,Real.class);
                List<Attach> attaches = rRecent.getAttaches()
                        .stream()
                        .map(e->{
                            Attach temp = modelMapper.map(e,Attach.class);
                            temp.setBoardReferenceType(BoardReferenceType.REAL);
                            temp.setBoardSeq(putDto.getBdSeq());
                            return temp;
                        })
                        .collect(Collectors.toList());
                real.setAttaches(attaches);
                real.setBoard(board);
                real.setUpdateAt(new Date());
                realRepository.save(real);
            }
        }
        return true;
    }


    /*
        작성자: 홍민석
        작성일: 19-12-02
        내용: REAL THINK 생성시 FreePass권 1회 소모.
        프리패스권(리얼티켓)이 모자라면 예외처리.
     */
    @Transactional
    protected boolean useFreePass(){
        User user = userRepository.findUserByEmail(findEmailBySpringSecurity());
        if(user.getRealTicket()<=0) throw new MoneyException("리얼티켓 개수 부족");
        user.setRealTicket(user.getRealTicket() - 1);
        userRepository.save(user);
        return true;
    }


    public boolean isHotThink(long bdSeq){
        Board board = boardRepository.findBoardByBdSeq(bdSeq);
        if(!board.getBoardType().equals(BoardType.HOT)) throw new IdeaInvalidException();

        return true;
    }
    /*
        작성자: 홍민석
        작성일: 19-10-22
        내용: 게시물을 삭제합니다.
        작성일: 19-11-01
        내용: 권한 인증 코드 작성
     */
    @Transactional
    public <Tin extends BoardInDTO> boolean deleteOne(Long seq, Tin inDto) {
        Board board = boardRepository.findBoardByBdSeq(seq);
        if(!isWriter(board.getUser().getEmail()))
            throw new UserUnauthorizedException("Access Deny");
        switch(board.getBoardType()){
            case REAL:
                board.setUseAt(UseAt.N);
                boardRepository.save(board);
                break;
            default:
                boardRepository.delete(board);
                break;
        }
        return true;
    }
    /*
        작성자: 홍민석
        작성일: 19-10-20
        내용: freethink 게시물 좋아요 기능
        TB_LIKE에 좋아요 기록을 표시한 후,
        게시판 좋아요(good)을 1만큼 증가시킵니다.
        작성일: 19-11-13
        내용: 프론트 요청으로 email return
    */
    @Transactional
    public String setLike(LikeDTO likeDto) {
        String email;
        User user = userRepository.findUserByEmail(findEmailBySpringSecurity());
        Like repeat = likeRepository.findByBdSeqAndBoardTypeAndUser(likeDto.getSeq(),likeDto.getBoardType(),user);
        if(repeat != null) throw new LikeConflictException();

        Like like = Like.ByCreateBuilder()
                .bdSeq(likeDto.getSeq())
                .boardType(likeDto.getBoardType())
                .user(user)
                .build();
        switch(likeDto.getBoardType()){
            case FREE:
                Board board = boardRepository.findBoardByBdSeq(likeDto.getSeq());
                email = board.getUser().getEmail();
                break;
            case REPLY:
                Reply reply = replyRepository.findReplyByRpSeq(likeDto.getSeq());
                email = reply.getUser().getEmail();
                break;
            default:
                throw new IdeaInvalidException("Invalid Board Type Exception");
        }
        //if(isWriter(email))throw new UserConflictException("자기자신 좋아요",ErrorCode.EMAIL_CONFLICT);
        likeRepository.save(like);
        return email;
    }

    /*
        작성자: 홍민석
        작성일: 19-11-26
        내용: 좋아요 리스트 반환 함수 작성
     */
    public List<LikeOutDTO> getLikeList(LikeDTO likeDTO){
        User user = userRepository.findUserByEmail(findEmailBySpringSecurity());
        List<LikeOutDTO> likes = likeRepository.findAllByBdSeqAndBoardTypeAndUser(likeDTO.getSeq(),likeDTO.getBoardType(),user)
                .stream()
                .map(e->modelMapper.map(e,LikeOutDTO.class))
                .collect(Collectors.toList());
        return likes;
    }

    /*
        작성자: 홍민석
        작성일: 19-11-12
        내용: 좋아요 취소
        작성일: 19-11-13
        내용: 프론트 요청으로 Email값 return.
     */
    @Transactional
    public <Tin extends BoardInDTO> String deleteLike(LikeDTO likeDTO) {
        User user = userRepository.findUserByEmail(findEmailBySpringSecurity());
        Like like = likeRepository.findByBdSeqAndBoardTypeAndUser(likeDTO.getSeq(),likeDTO.getBoardType(),user);
        if(like==null)
            throw new LikeNotFoundException("Like Not Found Exception");
        likeRepository.delete(like);
        return user.getEmail();
    }
    /*
        작성자: 홍민석
        작성일: 19-10-07
        내용: freethink 게시물 번호를 인자로 받고
        게시물과 관련된 모든 댓글을 반환합니다.
    */
    @Transactional
    public List<ReplyOutDTO> getReplyList(Long bdSeq) {
        return replyRepository.findReplyByBdSeq(bdSeq)
                .stream()
                .map(s->modelMapper.map(s, ReplyOutDTO.class))
                .collect(Collectors.toList());
    }

    /*
        작성자: 홍민석
        작성일: 19-10-07
        내용: 댓글 작성, bdSeq와 category는 ReplyInDTO 클래스에 존재.
    */
    @Transactional
    public boolean setReply(ReplyInDTO replyInDTO) {
        Reply fReply=null; // 댓글 객체 포인터

        Board board = boardRepository.findBoardByBdSeq(replyInDTO.getBdSeq());
        if(board==null) throw new IdeaNotFoundException();

        User user = userRepository.findUserByEmail(findEmailBySpringSecurity());
        if(user==null) throw new UserNotFoundException();

        if(replyInDTO.getSuperRpSeq()==null) {
            final Reply reply = Reply.BySetBuilder()
                    .contents(replyInDTO.getContents())
                    .board(board)
                    .user(user)
                    .build();
            fReply=reply;
        }else{
            Reply superReply = replyRepository.findReplyByRpSeq(replyInDTO.getSuperRpSeq());
            if(superReply.getSuperSeq()!=null) throw new ReplyInvalidException();
            long depth = replyRepository.countRepliesBySuperSeq(replyInDTO.getSuperRpSeq()) + 1;
            final Reply reply = Reply.ReReplyBuilder()
                    .contents(replyInDTO.getContents())
                    .board(board)
                    .user(user)
                    .superSeq(replyInDTO.getSuperRpSeq())
                    .depth((int)depth)
                    .build();
            fReply = reply;
        }
        if(fReply!=null) {
            replyRepository.save(fReply);
            if (replyRepository.save(fReply) == null) throw new ReplyNotFoundException();
            return true;
        }
        return false;
    }
    /*
            작성자: 홍민석
            작성일: 19-11-01
            내용: 댓글 수정
    */
    @Transactional
    public boolean putReply(ReplyPutDTO replyPutDTO, Long rpSeq) {
        Reply reply = replyRepository.findReplyByRpSeq(rpSeq);
        if(!isWriter(reply.getUser().getEmail()))
            throw new UserUnauthorizedException("Access Deny");
        //TODO: 권한 인증 코드 작성

        reply.setContents(replyPutDTO.getContents());
        replyRepository.save(reply);
        return true;
    }
    /*
            작성자: 홍민석
            작성일: 19-10-24
            내용: 글 번호, 댓글 번호를 인자로 전달받아
            해당하는 댓글을 삭제합니다.
            작성일: 19-11-01
            내용: 권한인증 코드 작성
    */
    public boolean deleteReply(Long bdSeq, long replyId) {
        Board board = boardRepository.findBoardByBdSeq(bdSeq);
        if(board == null) throw new IdeaNotFoundException();
        Reply reply = replyRepository.findReplyByRpSeqAndBoard(replyId,board);
        if(reply == null) throw new ReplyNotFoundException();
        if(!isWriter(reply.getUser().getEmail()))throw new UserUnauthorizedException("Access Deny");
        replyRepository.delete(reply);
        return true;
    }
    /*
     공통 매퍼
     */
    public <T, E> T convertTo(E source, Class<? extends T> classLiteral) {
        return modelMapper.map(source, classLiteral);
    }

    /*
        작성자: 홍민석
        작성일: 2019-11-01
        내용: 권한 인증
     */
    private static boolean isWriter(String email){
        return ((UserBase) SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                .getEmail().equals(email);
    }

    /*
        작성자: 홍민석
        작성일: 2019-11-03
        내용: 권한 인증
        로그인 안되어 있을시 예외처리
     */
    private static String findEmailBySpringSecurity(){
        String email = (String)((UserBase) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail().toString();
        if(email == null){ throw new UserUnauthorizedException("권한없는 사용자"); }
        return email;
    }

}
