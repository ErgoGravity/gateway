    /**
      * inputs in CreateTransferWrapRq: 0-> linkListTokenRepo, 1-> maintainerRepo
      * outputs in CreateTransferWrapRq: 0-> linkListTokenRepo, 1-> linkListElementRepo , 2-> maintainerRepo
      *
      * inputs in ChangeStatus: 0-> Signal, 1-> Signal forced input, 2-> linkListTokenRepo, 3-> linkListElementRepo
      * outputs in ChangeStatus: 0-> Signal forced output (tokenRepo), 1-> linkListTokenRepo
      *
      * inputs in Mint: 0-> Signal, 1-> Signal forced input, 2-> maintainerRepo
      * outputs in Mint: 0-> Signal forced output (tokenRepo), 1-> maintainerRepo, 2-> receiver
    */
    val linkListTokenRepo =
      s"""{
         |  val minValue = 1000000 // TODO: check minValue with node
         |  val check = {
         |    if (INPUTS(0).tokens(1)._1 == linkListNFTToken){ // create Transfer wrap request
         |       INPUTS(1).tokens(1)._1 == maintainerNFTToken,
         |
         |       linkListTokenOutput = OUTPUTS(0)
         |       linkListTokenOutput.tokens(0)._1 == linkListTokenRepoId,
         |       linkListTokenOutput.tokens(0)._2 == INPUTS(0).tokens(0)._2 - 1,
         |       linkListTokenOutput.tokens(1)._1 == linkListNFTToken,
         |       linkListTokenOutput.propositionBytes == SELF.propositionBytes,
         |       linkListTokenOutput.value == INPUTS(0).value - minValue,
         |
         |       linkListElementOutput = OUTPUTS(1)
         |       blake2b256(linkListElementOutput.propositionBytes) == linkListElementRepoContractHash,
         |
         |       OUTPUTS(2).tokens(1)._1 == maintainerNFTToken,
         |     }
         |    else if (INPUTS(0).tokens(1)._1 == signalTokenNFT  ){ // ChangeStatus
         |       linkListTokenOutput = OUTPUTS(1)
         |       linkListTokenOutput.tokens(0)._2 == INPUTS(2).tokens(0)._2 + 1,
         |       linkListTokenOutput.tokens(0)._1 == linkListTokenRepoId,
         |       linkListTokenOutput.tokens(1)._1 == linkListNFTToken,
         |       linkListTokenOutput.propositionBytes == SELF.propositionBytes,
         |       linkListTokenOutput.value == INPUTS(2).value + minValue,
         |
         |       INPUTS(2).propositionBytes == SELF.propositionBytes,
         |       blake2b256(INPUTS(3).propositionBytes) == linkListElementRepoContractHash,
         |     }
         |    else false
         |  }
         |
         |  sigmaProp (check)
         |}""".stripMargin
    val maintainerRepo =
      s"""{
         |val check = {
         |  if (INPUTS(0).tokens(1)._1 == linkListNFTToken){ // create Transfer wrap request
         |    INPUTS(0).tokens(0)._1 == linkListTokenRepoId,
         |    INPUTS(1).propositionBytes == SELF.propositionBytes,
         |    INPUTS(1).ID == SELF.ID,
         |    linkListTokenOutput = OUTPUTS(0)
         |    linkListElementOutput = OUTPUTS(1)
         |    maintainerOutput = OUTPUTS(2)
         |
         |    linkListTokenOutput.tokens(1)._1 == linkListNFTToken
         |    blake2b256(linkListElementOutput.propositionBytes) == linkListElementRepoContractHash,
         |    maintainerOutput.propositionBytes == SELF.propositionBytes,
         |
         |    val amount = linkListElementOutput.R5[BigInt].get
         |
         |    maintainerOutput.R4[Int].get == INPUTS(0).R4[Int].get,
         |    if (INPUTS(1).tokens.size > 1){
         |      maintainerOutput.value == INPUTS(1).value
         |      maintainerOutput.tokens(1)._1 == INPUTS(1).tokens(1)._1,
         |      maintainerOutput.tokens(1)._2 == INPUTS(1).tokens(1)._2 + amount,
         |    }
         |    else{
         |      maintainerOutput.value == INPUTS(1).value + amount,
         |    }
         |  }
         |  else if (INPUTS(0).tokens(1)._1 == signalTokenNFT){ // Mint
         |    blake2b256(INPUTS(0).propositionBytes) == signalRepoContractHash,
         |    INPUTS(2).propositionBytes == SELF.propositionBytes,
         |    INPUTS(2).ID == SELF.ID,
         |
         |    maintainerOutput = OUTPUTS(1)
         |    maintainerOutput.tokens(0)._1 == maintainerRepoId,
         |    maintainerOutput.tokens(1)._1 == maintainerNFTToken,
         |
         |    OUTPUTS(2).tokens(0)._1 == maintainerRepoId,
         |
         |    val amount = INPUTS(2).R5[BigInt].get
         |
         |    if (INPUTS(1).tokens.size > 1){
         |      maintainerOutput.tokens(1)._1 == INPUTS(2).tokens(1)._1,
         |      maintainerOutput.tokens(1)._2 == INPUTS(2).tokens(1)._2 - amount,
         |      maintainerOutput.value == INPUTS(2).value
         |    }
         |    else{
         |       maintainerOutput.value == INPUTS(2).value - amount,
         |       // TODO: get receiver address from signal data
         |
         |    }
         |  }
         |}
         |  sigmaProp (check)
         |}""".stripMargin
    val linkListElementRepo =
      s"""{
         |  val minValue = 1000000 // TODO: check minValue with node
         |  val check = {
         |    if (INPUTS(0).tokens(1)._1 == linkListNFTToken){ // create Transfer wrap request
         |       INPUTS(1).tokens(1)._1 == maintainerNFTToken,
         |
         |       linkListTokenOutput = OUTPUTS(0),
         |       linkListTokenOutput.tokens(1)._1 == linkListNFTToken,
         |
         |       linkListElementOutput = OUTPUTS(1),
         |       linkListElementOutput.propositionBytes == SELF.propositionBytes,
         |       linkListElementOutput.tokens(0)._1 == linkListTokenRepoId,
         |       linkListElementOutput.tokens(0)._2 == 1,
         |       linkListElementOutput.R4[Coll[Byte]].isDefined, // receiver address
         |       linkListElementOutput.R5[BigInt].isDefined, // request amount
         |       linkListElementOutput.value == minValue,
         |
         |       OUTPUTS(2).tokens(1)._1 == maintainerNFTToken,
         |     }
         |    else if (INPUTS(0).tokens(1)._1 == signalTokenNFT){ // approve
         |       INPUTS(2).tokens(1)._1 == linkListNFTToken,
         |       INPUTS(3).propositionBytes == SELF.propositionBytes,
         |       INPUTS(3).ID == SELF.ID,
         |     }
         |    else false
         |  }
         |
         |  sigmaProp (check)
         |}""".stripMargin
